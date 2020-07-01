package com.molo.gui;

import com.molo.entity.ChatMessage;

/**
 * Created by Molobala on 30/03/2017.
 */

public interface OnNewMessageListener {
    public void onNewMessage(com.molo.entity.ChatThread th,ChatMessage m);
    public void onMessageSent(com.molo.entity.ChatThread th,ChatMessage cm);
    public void onMessageSeen(com.molo.entity.ChatThread th);
}
