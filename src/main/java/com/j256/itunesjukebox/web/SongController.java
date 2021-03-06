package com.j256.itunesjukebox.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.springframework.beans.factory.annotation.Required;

import com.j256.itunesjukebox.applescript.Track;
import com.j256.itunesjukebox.auto.PlayListAutomation;
import com.j256.simplewebframework.freemarker.ModelView;
import com.j256.simplewebframework.params.SessionParam;

/**
 * Controller that handles song functions.
 * 
 * @author graywatson
 */
@Path("/songs")
@WebService
public class SongController {

	private static final SortSongByName sortSongByName = new SortSongByName();

	private AdminController adminController;
	private PlayListAutomation playListAutomation;

	@Path("/")
	@GET
	@WebMethod
	public ModelView songs() {
		return search(null);
	}

	@Path("/search")
	@GET
	@WebMethod
	public ModelView search(@QueryParam("query") String query) {
		Track[] tracks = adminController.getTracks();
		if (tracks == null) {
			System.err.println("No tracks returned");
			return null;
		}
		List<Track> searchedTracks = new ArrayList<Track>();
		if (query != null) {
			query = query.toLowerCase();
		}
		for (Track track : tracks) {
			if (query == null || track.getName().toLowerCase().contains(query)) {
				searchedTracks.add(track);
			}
		}
		Collections.sort(searchedTracks, sortSongByName);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("tracks", searchedTracks);
		return new ModelView(model, ViewConstants.SONGS);
	}

	@Path("/played")
	@GET
	@WebMethod
	public ModelView played() {
		List<Track> playedTracks = playListAutomation.getPlayedTracks();
		if (playedTracks == null) {
			System.err.println("No tracks returned");
			return null;
		}

		// reverse the order so the most recent played are top of the list
		List<Track> results = new ArrayList<Track>(playedTracks);
		Collections.reverse(results);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("tracks", results);
		return new ModelView(model, ViewConstants.SONGS);
	}

	@Path("/one")
	@GET
	@WebMethod
	public ModelView song(@SessionParam HttpSession session, @QueryParam("id") int id,
			@Context HttpServletResponse response) {
		Track[] trackInfos = adminController.getTracks();
		Track found = null;
		for (Track track : trackInfos) {
			if (track.getId() == id) {
				found = track;
				break;
			}
		}
		if (found == null) {
			throw new IllegalArgumentException("Could not find track id #" + id);
		}

		addNoCacheHeaders(response);
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("track", found);
		model.put("admin", (session.getAttribute(SessionConstants.ADMIN) != null));
		return new ModelView(model, ViewConstants.SONGS_ONE);
	}

	@Required
	public void setAdminController(AdminController adminController) {
		this.adminController = adminController;
	}

	@Required
	public void setPlayListAutomation(PlayListAutomation playListAutomation) {
		this.playListAutomation = playListAutomation;
	}

	private void addNoCacheHeaders(HttpServletResponse response) {
		response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		response.addHeader("Pragma", "no-cache");
		response.addHeader("Expires", "0");
	}

	public static class SortSongByName implements Comparator<Track> {

		@Override
		public int compare(Track o1, Track o2) {
			return o1.getName().compareTo(o2.getName());
		}
	}
}
