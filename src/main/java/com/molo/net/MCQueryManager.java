package com.molo.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.molo.ftp.MFtp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Molobala on 29/03/2017.
 */

public class MCQueryManager {
    public WeakReference<Client> client;
    public static List<MCQuery> request=new ArrayList<>();
    public static ObjectMapper mapper=new ObjectMapper();
    public MCQueryManager(Client client){
        this.client=new WeakReference<Client>(client);
        client.registerManager(this);
        MCQuery.configure(this);
    }
    public Client getClient(){
        return client.get();
    }

    public void dispose(){
        request.clear();
    }
    public void onResult(Response r) {
        synchronized (request){
            for(MCQuery q:request){
                if(r.hash==q.hash){
                    request.remove(q);
                    q.onCallBack(r);
                    break;
                }
            }
        }
    }

    public void pushQuery(MCQuery mcQuery) {
        synchronized (request){
            request.add(mcQuery);
        }
    }

    synchronized public void execute(MCQuery mcQuery) {
        client.get().writeToSocket(mcQuery.toString());
        if(mcQuery.data.has("files")){
        	try {
        		MediaFile[] files=mapper.readValue(mcQuery.data.get("files").toString(), MediaFile[].class);
				for(MediaFile m:files){
					DataOutputStream out=client.get().getOut();
					if(out!=null)
					{
						System.out.println("OU non null");
						MFtp.ftpPut(m.absolutPath,out );
					}else{
						System.out.println("OUT NULL");
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
}
