package com.molo.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Molobala on 29/03/2017.
 * That class represents a query *****
 */

public class MCQuery {
    private static long count=1;
    public static ObjectMapper mapper=new ObjectMapper();
    public String query;
    public ObjectNode data;
    public long hash;
    public OnMCQueryResult callback;
    public static MCQueryManager manager;
    public List<MediaFile> medias=null;
    public static void configure(MCQueryManager m){
        manager=m;
    }
    public MCQuery(String query,ObjectNode data){
        this.data=data;
        this.query=query;
        hash=count++;
        data.put("hash",hash);
        data.put("command",query);
    }
    public MCQuery(String query){
        this.query=query;
        data=mapper.createObjectNode();
        hash=count++;
        data.put("hash",hash);
        data.put("command",query);
    }
    public void addFiles(List<File> m){
    	if(medias==null)
    		medias=new ArrayList<MediaFile>();
    	for(File f:m)
    	{
    		MediaFile mf=new MediaFile();
    		//mf.file=f;
    		mf.name=f.getName();
    		mf.absolutPath=f.getAbsolutePath();
    		mf.size=f.length();
    		mf.type=MediaFile.getFileType(f);
    		medias.add(mf);
    	}
    }
    public void addFile(File f){
    	if(medias==null)
    		medias=new ArrayList<MediaFile>();
    	MediaFile mf=new MediaFile();
		//mf.file=f;
		mf.name=f.getName();
		mf.absolutPath=f.getAbsolutePath();
		mf.size=f.length();
		mf.type=MediaFile.getFileType(f);
    	medias.add(mf);
    }
    public void addFile(MediaFile mf){
    	if(medias==null)
    		medias=new ArrayList<MediaFile>();
    	medias.add(mf);
    }
    public OnMCQueryResult getCallback() {
        return callback;
    }
    void onCallBack(Response r){
        if(callback!=null){
        	callback.onSuccess(r);
        }
    }

    @Override
    public String toString() {
        return data.toString();
    }

    public void execute(){
    	if(medias!=null){
    		try {
    			data.put("files",  mapper.readTree(mapper.writeValueAsString(medias)));
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}    		
    	}
        manager.pushQuery(this);
        manager.execute(this);
    }
}
