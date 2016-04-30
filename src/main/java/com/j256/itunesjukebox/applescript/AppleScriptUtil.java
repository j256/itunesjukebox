package com.j256.itunesjukebox.applescript;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * Utility class which enables Applescript commands to be run.
 * 
 * @author graywatson
 */
public class AppleScriptUtil {

	private static final String MUSIC_VIDEO_KIND = "music video";
	private static final String APPLESCRIPT_COMMAND = "/usr/bin/osascript";

	private static final Pattern playListTracksPattern =
			Pattern.compile("\\{\\{(.+)\\}, \\{(.+)\\}, \\{(.+)\\}, \\{(.+)\\}, \\{(.+)\\}, "
					+ "\\{(.+)\\}, \\{(.+)\\}, \\{(.+)\\}, \\{(.+)\\}}\n");
	private static final Pattern playingPattern = Pattern.compile("\\{(\\d+), ([\\d.]+), (.+), ([\\d.]+)\\}\n");
	private static final Pattern locationsPattern = Pattern.compile("\\{\\{(.+)\\}\\}\n");
	private static final Pattern artworkPattern = Pattern.compile("\\{\"(.*)\", \"(.*)\"\\}\n");

	private static final Runtime runtime = Runtime.getRuntime();
	private static final Map<String, Integer> locationMap = new HashMap<String, Integer>();

	/**
	 * List playlists.
	 */
	public static List<String> listPlayLists() {
		String results = runCommand("get name of playlists");
		if (results == null) {
			return null;
		}
		String[] parts = split(results, "\", \"");
		if (parts.length > 0) {
			parts[0] = parts[0].substring(2);
			String last = parts[parts.length - 1];
			int index = last.indexOf('\"');
			if (index > 0) {
				parts[parts.length - 1] = last.substring(0, index);
			}
		}
		List<String> playlists = new ArrayList<String>();
		for (String part : parts) {
			playlists.add(part.trim());
		}
		return playlists;
	}

	/**
	 * Stop playing.
	 */
	public static void stop() {
		runCommand("stop");
	}

	/**
	 * Pause playing.
	 */
	public static void pause() {
		runCommand("pause");
	}

	/**
	 * Continue playing.
	 */
	public static void play() {
		runCommand("play");
	}

	public static void nextTrack() {
		runCommand("next track");
	}

	/**
	 * Raise the volume by 10%
	 */
	public static void setVolume(int percentage) {
		runCommand("set the sound volume to " + percentage);
	}

	/**
	 * Raise the volume by 10%
	 */
	public static void volumeUp() {
		runCommand("if sound volume < 100 then\n" //
				+ "set the sound volume to (sound volume + 10)\n" //
				+ "end if\n");
	}

	/**
	 * Lower the volume by 10%
	 */
	public static void volumeDown() {
		runCommand("if sound volume > 0 then\n" //
				+ "set the sound volume to (sound volume - 10)\n" //
				+ "end if\n");
	}

	/**
	 * Start playing.
	 */
	public static void playPlayList(String playList) {
		runCommand("play playlist \"" + playList + "\"");
	}

	/**
	 * Start playing.
	 */
	public static PlayerState getPlayerState() {
		String str = runCommand("player state");
		if (str != null) {
			str = str.trim();
		}
		return PlayerState.fromString(str);
	}

	/**
	 * Create a playlist if it doesn't exist already.
	 */
	public static void createPlayListIfNotExists(String playList) {
		runCommand("if user playlist \"" + playList + "\" exists then\n" //
				+ "else\n" //
				+ "make new user playlist with properties {name:\"" + playList + "\"}\n" //
				+ "end if\n");
	}

	/**
	 * Delete all tracks in a playlist.
	 */
	public static void clearPlayList(String playList) {
		runCommand("delete tracks of user playlist \"" + playList + "\"");
	}

	/**
	 * Delete a playlist.
	 */
	public static void deletePlayList(String playList) {
		runCommand("delete user playlist \"" + playList + "\"");
	}

	/**
	 * Disable shuffle for a playlist.
	 */
	public static void disableShuffle(String playList) {
		runCommand("set shuffle to false of playlist \"" + playList + "\"");
	}

	/**
	 * Disable song repeat for a playlist.
	 */
	public static void disableSongRepeat(String playList) {
		runCommand("set song repeat to one of playlist \"" + playList + "\"");
	}

	/**
	 * Copy tracks from a source play-list to a destination play-list.
	 */
	public static void addTrackToPlayList(String sourcePlayList, Track trackInfo, String destPlayList) {
		// copy (first track in playlist "..." whose id is ...) to end of user playlist "..."
		runCommand("duplicate track id " + trackInfo.getLibraryId() + " of user playlist \"" + sourcePlayList
				+ "\" to end of user playlist \"" + destPlayList + "\"");
	}

	/**
	 * Remove an index from the playlist.
	 */
	public static void removeIndexFromPlaylist(String playList, int index) {
		runCommand("delete track " + index + " of user playlist \"" + playList + "\"");
	}

	/**
	 * Move an index to the end of a playlist.
	 */
	public static void moveIndexToEndOfPlaylist(String playList, int index) {
		runCommand("set trk to track " + index + " of playlist \"" + playList + "\"\n" //
				+ "move trk to end of playlist \"" + playList + "\"");
	}

	/**
	 * Get the current playing information.
	 */
	public static PlayingInfo getPlayingInfo() {
		String results = runCommand("get {index, finish, location} of current track & {player position}");
		// System.out.println(results);
		Matcher matcher = playingPattern.matcher(results);
		if (!matcher.matches()) {
			System.err.println("Playing results not in the right form: " + results);
			return null;
		}
		Integer index = parseInt(matcher.group(1).trim(), "index");
		if (index == null) {
			System.out.println("Could not process playing info: " + results);
			return null;
		}
		Double val = parseDouble(matcher.group(2).trim(), "finish");
		if (val == null) {
			System.out.println("Could not process playing info: " + results);
			return null;
		}
		int finish = val.intValue();
		String location = matcher.group(3);
		int start = location.indexOf('\"') + 1;
		int end = location.lastIndexOf('\"');
		int id = locationToId(location.substring(start, end), false);
		val = parseDouble(matcher.group(4), "position");
		if (val == null) {
			System.out.println("Could not process playing info: " + results);
			return null;
		}
		int position = val.intValue();
		return new PlayingInfo(id, index, finish, position);
	}

	/**
	 * Get the track information from the playlist.
	 */
	public static Track[] getTracksFromPlaylist(String playList) {
		String results =
				runCommand("get the {id, index, name, artist, album artist, year, video kind, location, genre} "
						+ "of (every track in playlist \"" + playList + "\")");
		if (results == null) {
			System.err.println("No results from get tracks from playlist");
			return null;
		}
		// System.out.println(results);

		if (results.indexOf('{') < 0) {
			return new Track[0];
		}
		Matcher matcher = playListTracksPattern.matcher(results);
		if (!matcher.matches()) {
			System.err.println("Invalid form for tracks from playlist: " + results);
			return null;
		}

		int[] libraryIds = processNumberArray(matcher.group(1), "libraryIds");
		if (libraryIds == null) {
			System.err.println("Could not process results from get playlist: " + results);
			return null;
		}
		int[] indexes = processNumberArray(matcher.group(2), "indexes");
		if (indexes == null) {
			System.err.println("Could not process results from get playlist: " + results);
			return null;
		}
		String[] names = processStringArray(matcher.group(3), "\", \"");
		String[] artists = processStringArray(matcher.group(4), "\", \"");
		String[] albumArtists = processStringArray(matcher.group(5), "\", \"");
		int[] years = processNumberArray(matcher.group(6), "years");
		if (years == null) {
			System.err.println("Could not process results from get playlist: " + results);
			return null;
		}
		String[] videoKinds = processStringArray(matcher.group(7), ", ");
		String[] locations = processStringArray(matcher.group(8), "\", alias \"");
		String[] genres = processStringArray(matcher.group(9), "\", \"");

		if (!validateArray(libraryIds.length, libraryIds.length, "libraryIds")) {
			System.err.println("Could not process results from get playlist: " + results);
			return null;
		}
		if (!validateArray(libraryIds.length, indexes.length, "indexes")) {
			System.err.println("Could not process results from get playlist: " + results);
			return null;
		}
		if (!validateArray(libraryIds.length, names.length, "names")) {
			System.err.println("Could not process results from get playlist: " + results);
			return null;
		}
		if (!validateArray(libraryIds.length, artists.length, "artists")) {
			System.err.println("Could not process results from get playlist: " + results);
			return null;
		}
		if (!validateArray(libraryIds.length, albumArtists.length, "albumArtists")) {
			return null;
		}
		if (!validateArray(libraryIds.length, years.length, "years")) {
			System.err.println("Could not process results from get playlist: " + results);
			return null;
		}
		if (!validateArray(libraryIds.length, videoKinds.length, "videoKind")) {
			return null;
		}
		if (!validateArray(libraryIds.length, locations.length, "location")) {
			System.err.println("Could not process results from get playlist: " + results);
			return null;
		}
		if (!validateArray(libraryIds.length, genres.length, "genre")) {
			System.err.println("Could not process results from get playlist: " + results);
			return null;
		}

		Track[] trackInfos = new Track[libraryIds.length];

		for (int i = 0; i < trackInfos.length; i++) {
			int id = locationToId(locations[i], true);
			String artist = artists[i];
			if (StringUtils.isEmpty(artist)) {
				artist = albumArtists[i];
			}
			boolean isVideo = MUSIC_VIDEO_KIND.equalsIgnoreCase(videoKinds[i]);
			trackInfos[i] = new Track(id, libraryIds[i], indexes[i], fixMacString(names[i]), fixMacString(artist),
					fixMacString(genres[i]), years[i], isVideo);
		}
		return trackInfos;
	}

	/**
	 * Get the locations from the playlist.
	 */
	public static int[] getIdsPlaylist(String playList) {
		String results = runCommand("get the {location} of (every track in playlist \"" + playList + "\")");
		if (results == null) {
			System.err.println("No results from get locations from playlist");
			return null;
		}
		// System.out.println(results);

		if (results.indexOf('{') < 0) {
			return new int[0];
		}
		Matcher matcher = locationsPattern.matcher(results);
		if (!matcher.matches()) {
			System.err.println("Invalid form for get locations from playlist: " + results);
			return null;
		}

		String[] locations = processStringArray(matcher.group(1), "\", alias \"");
		// map the locations to ids
		int[] ids = new int[locations.length];
		for (int i = 0; i < locations.length; i++) {
			ids[i] = locationToId(locations[i], false);
		}
		return ids;
	}

	/**
	 * Load any track artwork from the disk.
	 */
	public static Artwork loadTrackArtwork(Track track, String playList) throws IOException {
		char leftDoubleAngleQuotation = 171; // character version of <<
		char rightDoubleAngleQuotation = 187; // character version of >>
		String results = runCommand(//
				"tell artwork 1 of track id " + track.getLibraryId() + " of user playlist \"" //
						+ playList + "\"\n" //
						+ "   set srcBytes to raw data\n" //
						+ "   if format is " + leftDoubleAngleQuotation + "class PNG " + rightDoubleAngleQuotation
						+ " then\n" //
						+ "      set ext to \"png\"\n" //
						+ "   else\n" //
						+ "      set ext to \"jpg\"\n" //
						+ "   end if\n" //
						+ "end tell\n" //
						+ "set fileName to (path to temporary items folder from user domain as string) & \"cover.\" & ext\n" //
						+ "set outFile to open for access file fileName with write permission\n" //
						+ "set eof outFile to 0\n"//
						+ "write srcBytes to outFile\n" //
						+ "close access outFile\n" //
						+ "set posixFilename to POSIX path of filename\n" //
						+ "{posixFilename, ext}\n");
		Matcher matcher = artworkPattern.matcher(results);
		if (!matcher.matches()) {
			return null;
		}
		String fileName = matcher.group(1);
		String ext = matcher.group(2);
		File inputFile = new File(fileName);
		FileInputStream fis = new FileInputStream(inputFile);
		ByteArrayOutputStream baos = new ByteArrayOutputStream((int) inputFile.length());
		try {
			byte[] buf = new byte[1024];
			while (true) {
				int numRead = fis.read(buf);
				if (numRead < 0) {
					break;
				}
				baos.write(buf, 0, numRead);
			}
		} finally {
			fis.close();
			inputFile.delete();
		}
		return new Artwork(ext, baos.toByteArray());
	}

	private static String[] processStringArray(String part, String separator) {
		String[] results = split(part, separator);
		if (results.length > 0) {
			if (results[0].startsWith("alias \"")) {
				results[0] = results[0].substring(7);
			} else {
				results[0] = results[0].substring(1);
			}
			String last = results[results.length - 1];
			results[results.length - 1] = last.substring(0, last.length() - 1);
		}
		return results;
	}

	private static int[] processNumberArray(String part, String label) {
		String[] parts = split(part, ", ");
		int[] results = convertToIntegers(parts, label);
		return results;

	}

	private static boolean validateArray(int expected, int results, String label) {
		if (results == expected) {
			return true;
		} else {
			System.err.println("Only got " + results + " " + label + ", instead of " + expected
					+ " from get tracks from playlist");
			return false;
		}
	}

	/**
	 * This is necessary to convert from "decomposed" string forms which encode "Beyonce" as "Beyonce'" where the accent
	 * is after the character to the "composed" form which is suitable for UTF-8 Java and HTML output.
	 */
	private static String fixMacString(String str) {
		return Normalizer.normalize(str, Form.NFC);
	}

	private static int[] convertToIntegers(String[] array, String label) {
		int[] results = new int[array.length];
		for (int i = 0; i < results.length; i++) {
			Integer number = parseInt(array[i], label);
			if (number == null) {
				return null;
			} else {
				results[i] = number;
			}
		}
		return results;
	}

	private static Integer parseInt(String str, String label) {
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException nfe) {
			System.err.println("Trying to convert number '" + str + "' to number failed for " + label);
			return null;
		}
	}

	private static Double parseDouble(String str, String label) {
		try {
			return Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			System.err.println("Trying to convert number '" + str + "' to number failed for " + label);
			return null;
		}
	}

	private static String runCommand(String command) {
		Process process = null;
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("tell application \"iTunes\"\n");
			sb.append(command);
			if (!command.endsWith("\n")) {
				sb.append('\n');
			}
			sb.append("end tell\n");
			process = runtime.exec(new String[] { APPLESCRIPT_COMMAND, "-s", "s", "-e", sb.toString() }, null);
			String results = readResults(process.getInputStream());
			String errors = readResults(process.getErrorStream());
			if (errors != null && errors.length() > 0) {
				System.err.println("Applescript command returned: " + errors);
			}
			return results;
		} catch (IOException ioe) {
			return null;
		} finally {
			try {
				if (process != null) {
					process.waitFor();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}

	}

	private static String readResults(InputStream stream) throws IOException {
		InputStreamReader reader = new InputStreamReader(stream);
		StringBuilder sb = new StringBuilder();
		char[] buf = new char[1024];
		while (true) {
			int numRead = reader.read(buf);
			if (numRead < 0) {
				break;
			}
			sb.append(buf, 0, numRead);
		}
		return sb.toString();
	}

	/**
	 * We had to do this because we needed a whole-separator all fields method not in StringUtils.
	 */
	private static String[] split(String source, String sepStr) {
		List<String> results = new ArrayList<String>();
		int len = sepStr.length();
		int pos = 0;
		while (true) {
			int index = source.indexOf(sepStr, pos);
			if (index < 0) {
				break;
			}
			results.add(source.substring(pos, index));
			pos = index + len;
		}
		if (pos < source.length()) {
			results.add(source.substring(pos));
		}
		return results.toArray(new String[results.size()]);
	}

	private static int locationToId(String location, boolean addAllowed) {
		Integer id = locationMap.get(location);
		if (id != null) {
			return id;
		} else if (addAllowed) {
			// the id is the current size of the location-map, might as well use that unique number
			int newId = locationMap.size();
			locationMap.put(location, newId);
			// System.out.println("added location " + location + " as id " + newId);
			return newId;
		} else {
			System.err.println("Unknown location: " + location);
			return -1;
		}
	}
}
