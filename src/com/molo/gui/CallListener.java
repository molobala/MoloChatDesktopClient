package com.molo.gui;

import com.molo.entity.Membre;

public interface CallListener {
	public void onCallFrom(com.molo.entity.ChatThread m);
	public void onCallAccepted(com.molo.entity.ChatThread m,String label);
	public void onCallCanceled(com.molo.entity.ChatThread m);
	public void onCallFinished(com.molo.entity.ChatThread c);
}
