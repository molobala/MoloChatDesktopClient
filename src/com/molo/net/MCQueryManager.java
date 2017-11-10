package com.molo.net;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import com.molo.ftp.MFtp;

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
        		MediaFile[] files=mapper.readValue(mcQuery.data.get("files"),MediaFile[].class);
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
