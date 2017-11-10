package com.molo.net;

import java.io.File;

import com.molo.Utils;

public class MediaFile {
	//File file;
	public String type;
	public long size;
	public String name;
	public  String absolutPath;
	public static String getFileType(String fname){
		return Utils.getFileType(fname);
	}
	public static String getFileType(File f) {
		// TODO Auto-generated method stub
		return Utils.getFileType(f.getName());
	}
//	public File getFile() {
//		return file;
//	}
//	public void setFile(File file) {
//		this.file = file;
//	}
	
}
