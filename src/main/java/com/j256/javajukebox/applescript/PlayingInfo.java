package com.j256.javajukebox.applescript;

/**
 * Track information.
 * 
 * @author graywatson
 */
public class PlayingInfo {

	private final int id;
	private final int index;
	private final int finishSecs;
	private final int positionSecs;

	public PlayingInfo(int id, int index, int finishSecs, int positionSecs) {
		this.id = id;
		this.index = index;
		this.finishSecs = finishSecs;
		this.positionSecs = positionSecs;
	}

	public int getId() {
		return id;
	}

	public int getIndex() {
		return index;
	}

	public int getFinishSecs() {
		return finishSecs;
	}

	public int getPositionSecs() {
		return positionSecs;
	}

	@Override
	public String toString() {
		return "PlayingInfo [id=" + id + ", index=" + index + ", finishSecs=" + finishSecs + ", positionSecs="
				+ positionSecs + "]";
	}
}
