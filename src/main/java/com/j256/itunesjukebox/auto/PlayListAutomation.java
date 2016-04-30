package com.j256.itunesjukebox.auto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import com.j256.itunesjukebox.applescript.AppleScriptUtil;
import com.j256.itunesjukebox.applescript.PlayerState;
import com.j256.itunesjukebox.applescript.PlayingInfo;
import com.j256.itunesjukebox.applescript.Track;
import com.j256.itunesjukebox.web.AdminController;

/**
 * Does some automated playlist maintenance.
 * 
 * @author graywatson
 */
public class PlayListAutomation implements InitializingBean, DisposableBean, Runnable {

	private static final long THREAD_SLEEP_MILLIS = 5000;
	/** don't repeat the same artist randomly in 5 tracks */
	private static final int NO_ARTIST_REPEAT = 10;
	/** don't do another video for 5 tracks, should calculate based on frequency */
	private static final int NO_VIDEO_REPEAT = 5;
	/** keep this many random tracks in the queue */
	private static final int KEEP_THIS_MANY_QUEUED = 5;
	/** don't reorder future queued tracks if we are close to the end of the track */
	private static final int NO_CHANGES_IF_CLOSE_TO_END_SECS = 20;

	private AdminController adminController;

	private Thread thread;
	private final List<Track> playedList = new ArrayList<Track>();
	private final Random random = new Random();
	private volatile Track[] lastWorkingTracks = new Track[0];

	@Override
	public void afterPropertiesSet() {
		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void destroy() {
		if (thread != null) {
			thread.interrupt();
			try {
				thread.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public void run() {
		Thread thread = Thread.currentThread();
		while (!thread.isInterrupted()) {

			try {
				Thread.sleep(THREAD_SLEEP_MILLIS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}

			doMaintenance();
		}
	}

	public Track[] getWorkingTracks() {
		return lastWorkingTracks;
	}

	public List<Track> getPlayedTracks() {
		return playedList;
	}

	@Required
	public void setAdminController(AdminController adminController) {
		this.adminController = adminController;
	}

	private void doMaintenance() {
		Track[] sourceTracks = adminController.getTracks();
		if (sourceTracks == null) {
			return;
		}

		Map<Integer, Track> sourceTrackMap = new HashMap<Integer, Track>();
		for (Track track : sourceTracks) {
			sourceTrackMap.put(track.getId(), track);
		}

		// see if there is a running tracks
		PlayingInfo playingInfo = AppleScriptUtil.getPlayingInfo();
		if (playingInfo != null) {
			if (lastWorkingTracks.length > playingInfo.getIndex() - 1) {
				Track playingTrack = lastWorkingTracks[playingInfo.getIndex() - 1];
				System.out.println("Playing status is: " + playingInfo + ", track: \"" + playingTrack.getName()
						+ "\" by \"" + playingTrack.getArtist() + "\"");
			} else {
				System.out.println("Playing status is: " + playingInfo);
			}
			if (playingInfo.getId() < 0) {
				// we aren't playing the right playlist all of a sudden, bail
				System.err.println("ERROR: We aren't playing in playlist: " + adminController.getTmpPlayList());
				return;
			}
			// remove any other tracks before it, adding 1 to their play-count
			for (int i = 1; i < playingInfo.getIndex(); i++) {
				// remove the first one X times, not i
				AppleScriptUtil.removeIndexFromPlaylist(adminController.getTmpPlayList(), 1);
				int workingIndex = i - 1;
				if (workingIndex < lastWorkingTracks.length) {
					Track justPlayed = lastWorkingTracks[workingIndex];
					justPlayed.incrementPlayCount();
					System.out.println("Removed index " + i + ": " + justPlayed);
					// we clear the votes so people need to vote it up to get on the list again
					justPlayed.clearVoteCount();
					playedList.add(justPlayed);
				} else {
					// shouldn't happen but let's be careful
					System.out.println("Removed index " + i);
				}
			}
		}

		/*
		 * First off, put anything that has votes in the list into playlist, sorted by number of votes, highest first.
		 * NOTE: the votes will get cleared once it gets played
		 */
		List<Track> toAdd = new ArrayList<Track>();
		for (Track track : sourceTracks) {
			if (track.getVoteCount() > 0) {
				toAdd.add(track);
			}
		}
		// now remove any of the tracks that are already in the working queue
		for (Track track : lastWorkingTracks) {
			toAdd.remove(track);
		}

		// any new voted on tracks to add to the list
		if (toAdd.size() > 0) {
			Collections.sort(toAdd);
			for (Track track : toAdd) {
				AppleScriptUtil.addTrackToPlayList(adminController.getSourcePlayList(), track,
						adminController.getTmpPlayList());
				System.out.println("Added track: " + track);
			}
		}

		// do we have enough tacks in our current playlist?
		Track[] workingTracks;
		while (true) {
			int[] ids = AppleScriptUtil.getIdsPlaylist(adminController.getTmpPlayList());
			if (ids == null) {
				return;
			}
			// turn the working tracks into pointers to our source tracks
			workingTracks = new Track[ids.length];
			for (int i = 0; i < ids.length; i++) {
				workingTracks[i] = sourceTrackMap.get(ids[i]);
			}
			// 1 for playing and X queued
			if (workingTracks.length >= 1 + KEEP_THIS_MANY_QUEUED) {
				break;
			}
			addRandomTrack(sourceTracks, workingTracks);
		}
		lastWorkingTracks = workingTracks;

		// now that we have maybe added some tracks to the playlist, see if we need to start the party
		if (playingInfo == null && AppleScriptUtil.getPlayerState() == PlayerState.STOPPED) {
			AppleScriptUtil.playPlayList(adminController.getTmpPlayList());
			System.out.println("Playing out playlist");
		}

		// get playing information again now that we might have removed or added
		playingInfo = AppleScriptUtil.getPlayingInfo();

		// if no playing information or we are close to the end of the track, then stop
		if (playingInfo == null
				|| playingInfo.getPositionSecs() + NO_CHANGES_IF_CLOSE_TO_END_SECS >= playingInfo.getFinishSecs()) {
			return;
		}

		/*
		 * Sort the queued tracks in case people are voting on them. We are using bubble sort but it's a small list so
		 * that fine. What confuses matters is that the only applescript command we have is to move an index to the end
		 * of the list. So we are going to have to loop around more often then we'd like.
		 */
		int i = playingInfo.getIndex();
		while (i < workingTracks.length - 1) {
			Track track = workingTracks[i];
			boolean foundBetter = false;
			for (int j = i + 1; j < workingTracks.length; j++) {
				if (track.compareTo(workingTracks[j]) > 0) {
					foundBetter = true;
					break;
				}
			}
			if (foundBetter) {
				// if we know that this track is the highest then we throw it to the _end_ of the list, blah
				AppleScriptUtil.moveIndexToEndOfPlaylist(adminController.getTmpPlayList(), i + 1);
				System.out.println("Moved track to end: " + track);
				// shift all of them down one
				for (int k = i; k < workingTracks.length - 1; k++) {
					workingTracks[k] = workingTracks[k + 1];
				}
				// put the one we know is _not_ the best at the end the list
				workingTracks[workingTracks.length - 1] = track;
				// restart the process
			} else {
				i++;
			}
		}
	}

	private void addRandomTrack(Track[] sourceTracks, Track[] workingTracks) {

		// now make sure we have at good number queued + the one playing
		int needed = (KEEP_THIS_MANY_QUEUED + 1) - workingTracks.length;
		if (needed <= 0) {
			return;
		}

		// now go over the source tracks and find the lowest play count (usually 0)
		int lowestPlayCount = Integer.MAX_VALUE;
		for (Track track : sourceTracks) {
			if (track.getPlayCount() < lowestPlayCount) {
				lowestPlayCount = track.getPlayCount();
			}
		}

		// find all of the tracks with that low play count
		List<Track> available = new ArrayList<Track>();
		for (Track track : sourceTracks) {
			if (track.getPlayCount() == lowestPlayCount) {
				available.add(track);
			}
		}

		// shouldn't happen but let's be careful out there
		if (available.isEmpty()) {
			return;
		}
		// we get an emergency pick in case we remove everything below
		Track emergencyPick = available.get(random.nextInt(available.size()));

		Iterator<Track> iterator = available.iterator();
		OUTER : while (iterator.hasNext()) {
			Track track = iterator.next();

			// working tracks that are similar to ours
			for (Track workingTrack : workingTracks) {
				if (track.getArtist().equals(workingTrack.getArtist())) {
					iterator.remove();
					continue OUTER;
				}
				if (track.isVideo() && workingTrack.isVideo()) {
					iterator.remove();
					continue OUTER;
				}
			}
			// remove it if the same artist recently
			for (int i = playedList.size() - 1; i >= 0 && i >= playedList.size() - NO_ARTIST_REPEAT; i--) {
				if (track.getArtist().equals(playedList.get(i).getArtist())) {
					iterator.remove();
					continue OUTER;
				}
			}

			// remove it if we played a video recently
			if (track.isVideo()) {
				for (int i = playedList.size() - 1; i >= 0 && i >= playedList.size() - NO_VIDEO_REPEAT; i--) {
					if (playedList.get(i).isVideo()) {
						iterator.remove();
						continue OUTER;
					}
				}
			}
		}

		if (available.isEmpty()) {
			AppleScriptUtil.addTrackToPlayList(adminController.getSourcePlayList(), emergencyPick,
					adminController.getTmpPlayList());
			System.out.println("Added emergency pick: " + emergencyPick);
			return;
		}

		Track track = available.remove(random.nextInt(available.size()));
		AppleScriptUtil.addTrackToPlayList(adminController.getSourcePlayList(), track,
				adminController.getTmpPlayList());
		System.out.println("Added track: " + track);
	}
}
