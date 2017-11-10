package com.molo.audio;

import java.io.File;


public interface AudioRecordListener {
	public void onRecordStart();
	public void onRecordFinish(File f);
	public void onRecordFailed();
	public void onTimer(long t,byte[] data, int bytesRead);
}
