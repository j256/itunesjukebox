package com.j256.itunesjukebox.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.io.Buffer;
import org.springframework.beans.factory.annotation.Required;

import com.j256.itunesjukebox.applescript.Artwork;
import com.j256.itunesjukebox.applescript.Track;
import com.j256.simplewebframework.displayer.BinaryResultDisplayer.BinaryResult;
import com.j256.simplewebframework.freemarker.ModelView;

/**
 * Controller that handles /.
 * 
 * @author graywatson
 */
@WebService
public class RootController {

	private static final MimeTypes mimeTypes = new MimeTypes();

	private AdminController adminController;

	@Path("/")
	@GET
	@WebMethod
	public ModelView root() {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("initialized", (adminController.getTracks() != null));
		return new ModelView(model, ViewConstants.ROOT);
	}

	@Path("/artwork")
	@GET
	@WebMethod
	public BinaryResult artwork(@QueryParam("id") int id) {
		Track track = adminController.getTrackById(id);
		if (track == null) {
			throw new IllegalArgumentException("Could not find track id #" + id);
		}
		Artwork artwork;
		try {
			artwork = track.getOrLoadArtwork(adminController.getSourcePlayList());
			if (artwork == null) {
				System.err.println("Track does not have artwork " + track);
				return null;
			}
		} catch (IOException ioe) {
			System.err.println("Problems loading artwork for track: " + track);
			ioe.printStackTrace(System.err);
			return null;
		}

		Buffer mime = mimeTypes.getMimeByExtension("image." + artwork.getExt());
		String contentType = null;
		if (mime != null) {
			contentType = mime.toString();
		}

		return new BinaryResult(contentType, artwork.getBytes());
	}

	@Path("/videos.html")
	@GET
	@WebMethod
	public ModelView all() {
		Track[] tracks = adminController.getTracks();
		if (tracks == null) {
			System.err.println("No tracks returned");
			return null;
		}
		List<Track> videoTracks = new ArrayList<Track>();
		for (Track track : tracks) {
			if (track.isVideo()) {
				videoTracks.add(track);
			}
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("videoTracks", videoTracks);
		return new ModelView(model, ViewConstants.VIDEOS);
	}

	@Required
	public void setAdminController(AdminController adminController) {
		this.adminController = adminController;
	}
}
