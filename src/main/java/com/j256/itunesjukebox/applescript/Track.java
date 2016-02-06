package com.j256.itunesjukebox.applescript;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Track information.
 * 
 * @author graywatson
 */
public class Track implements Comparable<Track> {

	private static final Artwork NO_ARTWORK = new Artwork(null, null);

	private final int id;
	private final int libraryId;
	private final int index;
	private final String name;
	private final String artist;
	private final String genre;
	private final int year;
	private final AtomicInteger playCount = new AtomicInteger();
	private final AtomicInteger voteCount = new AtomicInteger();
	private final boolean isVideo;
	private transient Artwork artwork;

	public Track(int id, int libraryId, int index, String name, String artist, String genre, int year,
			boolean isVideo) {
		this.id = id;
		this.libraryId = libraryId;
		this.index = index;
		this.name = name;
		this.artist = artist;
		this.genre = genre;
		this.year = year;
		this.isVideo = isVideo;
	}

	public int getId() {
		return id;
	}

	public int getLibraryId() {
		return libraryId;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public String getArtist() {
		return artist;
	}

	public String getGenre() {
		return genre;
	}

	public int getYear() {
		return year;
	}

	public boolean isVideo() {
		return isVideo;
	}

	public int getPlayCount() {
		return playCount.get();
	}

	public void incrementPlayCount() {
		playCount.incrementAndGet();
	}

	public int getVoteCount() {
		return voteCount.get();
	}

	public void incrementVoteCount() {
		voteCount.incrementAndGet();
	}

	public void clearVoteCount() {
		voteCount.set(0);
	}

	public Artwork getOrLoadArtwork(String playList) throws IOException {
		if (artwork == null) {
			Artwork artwork = AppleScriptUtil.loadTrackArtwork(this, playList);
			if (artwork == null) {
				this.artwork = NO_ARTWORK;
			} else {
				this.artwork = artwork;
			}
		}
		return getArtwork();
	}

	public Artwork getArtwork() {
		if (artwork == NO_ARTWORK) {
			return null;
		} else {
			return artwork;
		}
	}

	public void setArtwork(Artwork artwork) {
		this.artwork = artwork;
	}

	@Override
	public int compareTo(Track other) {
		// higher votes go first
		int diff = other.voteCount.get() - voteCount.get();
		if (diff != 0) {
			return diff;
		}
		// fewer plays goes ahead of more
		return playCount.get() - other.playCount.get();
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		Track other = (Track) obj;
		return (id == other.id);
	}

	@Override
	public String toString() {
		return "Track [id=" + id + ", index=" + index + ", name=" + name + ", artist=" + artist + ", year=" + year
				+ ", plays=" + playCount.get() + ", votes=" + voteCount.get() + ", video=" + isVideo + "]";
	}
}
