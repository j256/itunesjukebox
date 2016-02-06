package com.j256.javajukebox.web;

/**
 * Some constants around view paths.
 * 
 * @author graywatson
 */
public class ViewConstants {

	private static final String CONTROLLER_ROOT = "/controller";

	public static final String ROOT = CONTROLLER_ROOT + "/index.html";
	public static final String ARTISTS_ALL = CONTROLLER_ROOT + "/artists/index.html";
	public static final String ARTIST_TRACKS = CONTROLLER_ROOT + "/artists/tracks.html";
	public static final String GENRES_ALL = CONTROLLER_ROOT + "/genres/index.html";
	public static final String GENRE_TRACKS = CONTROLLER_ROOT + "/genres/tracks.html";
	public static final String SONGS = CONTROLLER_ROOT + "/songs/index.html";
	public static final String SONGS_ONE = CONTROLLER_ROOT + "/songs/one.html";
	public static final String VIDEOS = CONTROLLER_ROOT + "/videos.html";
	public static final String SEARCH = CONTROLLER_ROOT + "/search.html";

	public static final String ADMIN = CONTROLLER_ROOT + "/admin/index.html";
	public static final String ADMIN_LOGIN = CONTROLLER_ROOT + "/admin/login.html";
	public static final String ADMIN_ERROR = CONTROLLER_ROOT + "/admin/error.html";
	public static final String PLAYLISTS = CONTROLLER_ROOT + "/admin/playlists.html";
	public static final String PLAYLIST_CHOSEN = CONTROLLER_ROOT + "/admin/chosen.html";
}
