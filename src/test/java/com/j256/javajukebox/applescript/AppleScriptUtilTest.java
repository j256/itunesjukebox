package com.j256.javajukebox.applescript;

import java.util.Arrays;

import org.junit.Test;

public class AppleScriptUtilTest {

	@Test
	public void testTracks() {
		Track[] tracks = AppleScriptUtil.getTracksFromPlaylist("JavaJukeBoxTmp");
		System.out.println(Arrays.toString(tracks));
	}

	@Test
	public void testPlayingInfo() {
		PlayingInfo playingInfo = AppleScriptUtil.getPlayingInfo();
		System.out.println(playingInfo.toString());
	}
}
