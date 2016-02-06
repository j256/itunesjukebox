package com.j256.javajukebox.web;

/**
 * Information about an artist, genre, or other thing.
 * 
 * @author graywatson
 */
public class NameInfo implements Comparable<NameInfo> {

	private String name;
	private int numTracks;

	public NameInfo(String name, int numTracks) {
		this.name = name;
		this.numTracks = numTracks;
	}

	public String getName() {
		return name;
	}

	public int getNumTracks() {
		return numTracks;
	}

	@Override
	public int compareTo(NameInfo other) {
		return name.compareTo(other.name);
	}
}
