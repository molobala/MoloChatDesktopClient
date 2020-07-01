package com.molo.audio;

import javax.sound.sampled.LineEvent;

public interface AudioPlayListener {
	public void onTimer(long currentTimeNanos);
}
