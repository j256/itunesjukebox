package com.j256.itunesjukebox.web;

import java.util.HashSet;
import java.util.Set;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.springframework.beans.factory.annotation.Required;

import com.j256.itunesjukebox.applescript.AppleScriptUtil;
import com.j256.itunesjukebox.applescript.Track;
import com.j256.itunesjukebox.auto.PlayListAutomation;
import com.j256.simplewebframework.params.SessionParam;

/**
 * Controller that handles Ajax Javascript calls.
 * 
 * @author graywatson
 */
@WebService
@Path("/ajax")
public class AjaxController {

	private AdminController adminController;
	private PlayListAutomation playListAutomation;

	@Path("/whatsPlaying")
	@GET
	@WebMethod
	@Produces("application/json")
	public Track[] whatsPlaying() {
		return playListAutomation.getWorkingTracks();
	}

	@Path("/vote")
	@GET
	@WebMethod
	public String vote(@QueryParam("id") int id, @SessionParam HttpSession session) {
		@SuppressWarnings("unchecked")
		Set<Integer> voteSet = (Set<Integer>) session.getAttribute(SessionConstants.VOTE_SET);
		if (voteSet == null) {
			voteSet = new HashSet<Integer>();
			session.setAttribute(SessionConstants.VOTE_SET, voteSet);
		}
		// see if someone is trying to double vote
		if (!voteSet.add(id) && session.getAttribute(SessionConstants.ADMIN) == null) {
			return "Session already voted for track " + id;
		}
		Track track = adminController.getTrackById(id);
		if (track == null) {
			System.err.println("Could not find track id #" + id);
			return "Could not find track id #" + id;
		} else {
			track.incrementVoteCount();
			return "track vote increased to " + track.getVoteCount();
		}
	}

	@Path("/pause")
	@GET
	@WebMethod
	public String pause() {
		AppleScriptUtil.pause();
		return "paused";
	}

	@Path("/play")
	@GET
	@WebMethod
	public String play() {
		AppleScriptUtil.play();
		return "playing";
	}

	@Path("/nextTrack")
	@GET
	@WebMethod
	public String nextTrack() {
		AppleScriptUtil.nextTrack();
		return "next track";
	}

	@Path("/volumeUp")
	@GET
	@WebMethod
	public String volumeUp() {
		AppleScriptUtil.volumeUp();
		return "volume up";
	}

	@Path("/volumeDown")
	@GET
	@WebMethod
	public String volumeDown() {
		AppleScriptUtil.volumeDown();
		return "volume down";
	}

	@Required
	public void setAdminController(AdminController adminController) {
		this.adminController = adminController;
	}

	@Required
	public void setPlayListAutomation(PlayListAutomation playListAutomation) {
		this.playListAutomation = playListAutomation;
	}

	public static class WhatsPlaying {

		final Track[] tracks;

		public WhatsPlaying(Track[] tracks) {
			this.tracks = tracks;
		}
	}
}
