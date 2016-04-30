package com.j256.itunesjukebox.web;

/**
 * Some constants around view paths.
 * 
 * @author graywatson
 */
public class ViewConstants {

	private static final String CONTROLLER_ROOT = "/controller";

	public static final String ROOT = CONTROLLER_ROOT + "/index.ftl";
	public static final String ARTISTS_ALL = CONTROLLER_ROOT + "/artists/index.ftl";
	public static final String ARTIST_TRACKS = CONTROLLER_ROOT + "/artists/tracks.ftl";
	public static final String GENRES_ALL = CONTROLLER_ROOT + "/genres/index.ftl";
	public static final String GENRE_TRACKS = CONTROLLER_ROOT + "/genres/tracks.ftl";
	public static final String YEARS_ALL = CONTROLLER_ROOT + "/years/index.ftl";
	public static final String YEAR_TRACKS = CONTROLLER_ROOT + "/years/tracks.ftl";
	public static final String SONGS = CONTROLLER_ROOT + "/songs/index.ftl";
	public static final String SONGS_ONE = CONTROLLER_ROOT + "/songs/one.ftl";
	public static final String VIDEOS = CONTROLLER_ROOT + "/videos.ftl";

	public static final String ADMIN = CONTROLLER_ROOT + "/admin/index.ftl";
	public static final String ADMIN_LOGIN = CONTROLLER_ROOT + "/admin/login.ftl";
	public static final String ADMIN_ERROR = CONTROLLER_ROOT + "/admin/error.ftl";
	public static final String PLAYLISTS = CONTROLLER_ROOT + "/admin/playlists.ftl";
	public static final String PLAYLIST_CHOSEN = CONTROLLER_ROOT + "/admin/chosen.ftl";
}
