package com.j256.itunesjukebox.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.servlet.http.HttpSession;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.j256.itunesjukebox.applescript.AppleScriptUtil;
import com.j256.itunesjukebox.applescript.Artwork;
import com.j256.itunesjukebox.applescript.Track;
import com.j256.simplewebframework.freemarker.ModelView;
import com.j256.simplewebframework.params.SessionParam;

/**
 * Controller that handles admin functions.
 * 
 * @author graywatson
 */
@Path("/admin")
@WebService
public class AdminController {

	private static final String DEFAULT_TMP_PLAYLIST = "iTunesJukeBoxTmp";
	public static final boolean LOAD_ARTWORK_AT_START = false;

	private volatile String adminPassword;
	private volatile String sourcePlayList;
	private volatile Track[] tracks;
	private volatile Map<Integer, Track> trackMap = new HashMap<Integer, Track>();

	private String tmpPlayList = DEFAULT_TMP_PLAYLIST;

	@Path("/")
	@GET
	@WebMethod
	public ModelView root(@SessionParam HttpSession session) {
		if (adminPassword == null || session.getAttribute(SessionConstants.ADMIN) == null) {
			return new ModelView("redirect:" + PathConstants.ADMIN_LOGIN);
		} else {
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("initialized", (tracks != null));
			return new ModelView(model, ViewConstants.ADMIN);
		}
	}

	@Path("/login/")
	@GET
	@WebMethod
	public ModelView login() {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("initialized", (tracks != null));
		return new ModelView(model, ViewConstants.ADMIN_LOGIN);
	}

	@Path("/login/submit")
	@POST
	@WebMethod
	public ModelView loginSubmit(@SessionParam HttpSession session, @QueryParam("password") String password) {
		String adminPassword = this.adminPassword;
		if (adminPassword == null) {
			this.adminPassword = password;
		} else if (!adminPassword.equals(password)) {
			System.err.println("Admin login is not valid");
			return new ModelView(ViewConstants.ADMIN_ERROR);
		}
		session.setAttribute(SessionConstants.ADMIN, "true");
		if (tracks == null) {
			return new ModelView("redirect:" + PathConstants.ADMIN_PLAYLISTS);
		} else {
			return new ModelView("redirect:" + PathConstants.ADMIN);
		}
	}

	@Path("/playlists/")
	@GET
	@WebMethod
	public ModelView playlists(@SessionParam HttpSession session) {
		if (session.getAttribute(SessionConstants.ADMIN) == null) {
			return new ModelView("redirect:" + PathConstants.ADMIN_LOGIN);
		}
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("playLists", AppleScriptUtil.listPlayLists());
		model.put("initialized", false);
		return new ModelView(model, ViewConstants.PLAYLISTS);
	}

	@Path("/playlists/chosen")
	@GET
	@WebMethod
	public ModelView choosePlaylist(@SessionParam HttpSession session, @FormParam("playList") String playList) {
		if (session.getAttribute(SessionConstants.ADMIN) == null) {
			return new ModelView("redirect:" + PathConstants.ADMIN_LOGIN);
		}

		AppleScriptUtil.stop();
		AppleScriptUtil.createPlayListIfNotExists(tmpPlayList);
		AppleScriptUtil.clearPlayList(tmpPlayList);
		AppleScriptUtil.disableShuffle(tmpPlayList);
		AppleScriptUtil.disableSongRepeat(tmpPlayList);

		sourcePlayList = playList;
		Track[] tracks = AppleScriptUtil.getTracksFromPlaylist(playList);
		Map<Integer, Track> trackMap = new HashMap<Integer, Track>();

		for (Track track : tracks) {
			trackMap.put(track.getId(), track);
			if (LOAD_ARTWORK_AT_START) {
				try {
					Artwork artwork = AppleScriptUtil.loadTrackArtwork(track, playList);
					if (artwork != null) {
						track.setArtwork(artwork);
					}
				} catch (IOException ioe) {
					System.err.println("Loading artwork for track got exception: " + track);
					ioe.printStackTrace(System.err);
				}
			}
		}

		this.tracks = tracks;
		this.trackMap = trackMap;

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("playlist", playList);
		model.put("tempPlaylist", tmpPlayList);
		model.put("tracks", tracks);
		return new ModelView(model, ViewConstants.PLAYLIST_CHOSEN);
	}

	public String getSourcePlayList() {
		return sourcePlayList;
	}

	public Track[] getTracks() {
		return tracks;
	}

	public Track getTrackById(int id) {
		return trackMap.get(id);
	}

	public String getTmpPlayList() {
		return tmpPlayList;
	}

	public void setTmpPlayList(String tmpPlayList) {
		this.tmpPlayList = tmpPlayList;
	}
}
