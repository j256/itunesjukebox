package com.j256.javajukebox.applescript;

/**
 * Track information.
 * 
 * @author graywatson
 */
public enum PlayerState {
	STOPPED("stopped"),
	PLAYING("playing"),
	PAUSED("paused"),
	UNKNOWN(""),
	// end
	;

	private final String appleScriptTag;

	private PlayerState(String appleScriptTag) {
		this.appleScriptTag = appleScriptTag;
	}

	/**
	 * Return a state from its string equivalent.
	 */
	public static PlayerState fromString(String appleScriptTag) {
		for (PlayerState playerState : values()) {
			if (playerState.appleScriptTag.equals(appleScriptTag)) {
				return playerState;
			}
		}
		return UNKNOWN;
	}
}
