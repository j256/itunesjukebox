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
 * Controller that handles year functions.
 * 
 * @author graywatson
 */
@WebService
@Path("/years")
public class YearController {

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
		Map<Integer, Integer> yearCountMap = new HashMap<Integer, Integer>();
		for (Track track : tracks) {
			if (track.getYear() == 0) {
				continue;
			}
			Integer count = yearCountMap.get(track.getYear());
			if (count == null) {
				count = 0;
			}
			yearCountMap.put(track.getYear(), count + 1);
		}

		List<NameInfo> yearInfos = new ArrayList<NameInfo>();
		for (Map.Entry<Integer, Integer> entry : yearCountMap.entrySet()) {
			yearInfos.add(new NameInfo(Integer.toString(entry.getKey()), entry.getValue()));
		}
		Collections.sort(yearInfos);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("yearInfos", yearInfos);
		return new ModelView(model, ViewConstants.YEARS_ALL);
	}

	@Path("/one")
	@GET
	@WebMethod
	public ModelView one(@QueryParam("year") int year) {
		Track[] tracks = adminController.getTracks();
		List<Track> yearTracks = new ArrayList<Track>();
		for (Track track : tracks) {
			if (track.getYear() == year) {
				yearTracks.add(track);
			}
		}
		Collections.sort(yearTracks, trackNameComparator);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("year", year);
		model.put("yearTracks", yearTracks);
		return new ModelView(model, ViewConstants.YEAR_TRACKS);
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
