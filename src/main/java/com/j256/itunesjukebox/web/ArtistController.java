package com.j256.itunesjukebox.web;

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

import com.j256.itunesjukebox.applescript.Track;
import com.j256.simplewebframework.freemarker.ModelView;

/**
 * Controller that handles artist functions.
 * 
 * @author graywatson
 */
@WebService
@Path("/artists")
public class ArtistController {

	private static TrackNameComparator trackNameComparator = new TrackNameComparator();

	private AdminController adminController;

	@Path("/")
	@GET
	@WebMethod
	public ModelView all() {
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
		if (query != null) {
			query = query.toLowerCase();
		}
		Map<String, Integer> artistCountMap = new HashMap<String, Integer>();
		for (Track track : tracks) {
			String artist = track.getArtist();
			if (query != null && !artist.toLowerCase().contains(query)) {
				continue;
			}
			Integer count = artistCountMap.get(artist);
			if (count == null) {
				count = 0;
			}
			artistCountMap.put(artist, count + 1);
		}

		List<NameInfo> artistInfos = new ArrayList<NameInfo>();
		for (Map.Entry<String, Integer> entry : artistCountMap.entrySet()) {
			artistInfos.add(new NameInfo(entry.getKey(), entry.getValue()));
		}
		Collections.sort(artistInfos);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("artistInfos", artistInfos);
		return new ModelView(model, ViewConstants.ARTISTS_ALL);
	}

	@Path("/one")
	@GET
	@WebMethod
	public ModelView one(@QueryParam("name") String name) {
		Track[] tracks = adminController.getTracks();
		List<Track> artistTracks = new ArrayList<Track>();
		for (Track track : tracks) {
			if (track.getArtist().equals(name)) {
				artistTracks.add(track);
			}
		}
		if (artistTracks.size() == 1) {
			return new ModelView("redirect:" + PathConstants.SONGS_ONE + "?id=" + artistTracks.get(0).getId());
		}
		Collections.sort(artistTracks, trackNameComparator);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("artistName", name);
		model.put("artistTracks", artistTracks);
		return new ModelView(model, ViewConstants.ARTIST_TRACKS);
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
