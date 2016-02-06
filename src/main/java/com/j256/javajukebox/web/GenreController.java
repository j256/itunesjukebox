package com.j256.javajukebox.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.springframework.beans.factory.annotation.Required;

import com.j256.javajukebox.applescript.Track;
import com.j256.simplewebframework.freemarker.ModelView;

/**
 * Controller that handles genre functions.
 * 
 * @author graywatson
 */
@WebService
@Path("/genres")
public class GenreController {

	private static TrackNameComparator trackNameComparator = new TrackNameComparator();

	private AdminController adminController;

	@Path("/")
	@GET
	@WebMethod
	public ModelView all() {
		Track[] tracks = adminController.getTracks();
		if (tracks == null) {
			System.err.println("No tracks returned");
			return null;
		}
		Map<String, Integer> artistCountMap = new HashMap<String, Integer>();
		for (Track track : tracks) {
			Integer count = artistCountMap.get(track.getGenre());
			if (count == null) {
				count = 0;
			}
			artistCountMap.put(track.getGenre(), count + 1);
		}

		List<NameInfo> genreInfos = new ArrayList<NameInfo>();
		for (Map.Entry<String, Integer> entry : artistCountMap.entrySet()) {
			genreInfos.add(new NameInfo(entry.getKey(), entry.getValue()));
		}
		Collections.sort(genreInfos);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("genreInfos", genreInfos);
		return new ModelView(model, ViewConstants.GENRES_ALL);
	}

	@Path("/one")
	@GET
	@WebMethod
	public ModelView one(@QueryParam("name") String name) {
		Track[] tracks = adminController.getTracks();
		List<Track> genreTracks = new ArrayList<Track>();
		for (Track track : tracks) {
			if (track.getGenre().equals(name)) {
				genreTracks.add(track);
			}
		}
		if (genreTracks.size() == 1) {
			return new ModelView("redirect:" + PathConstants.SONGS_ONE + "?id=" + genreTracks.get(0).getId());
		}
		Collections.sort(genreTracks, trackNameComparator);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("genreName", name);
		model.put("genreTracks", genreTracks);
		return new ModelView(model, ViewConstants.GENRE_TRACKS);
	}

	@Required
	public void setAdminController(AdminController adminController) {
		this.adminController = adminController;
	}

	private static class TrackNameComparator implements Comparator<Track> {
		@Override
		public int compare(Track o1, Track o2) {
			return o1.getName().compareTo(o2.getName());
		}
	}
}
