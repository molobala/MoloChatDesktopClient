package com.molo.gui;



import com.molo.entity.Membre;

/**
 * Created by Molobala on 30/03/2017.
 */

public interface OnMemberConnectionListener {
    public void onMemberConnected(Membre m);
    public void onMemberDisconnection(Membre m);
}
