package com.molo;

import java.io.File;

import com.molo.net.Client;

public class Utils {
	public static  String convertHexToString(String hex){

		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();

		//49204c6f7665204a617661 split into two characters 49, 20, 4c...
		for( int i=0; i<hex.length()-1; i+=2 ){

			//grab the hex in pairs
			String output = hex.substring(i, (i + 2));
			//convert hex to decimal
			int decimal = Integer.parseInt(output, 16);
			//convert the decimal to character
			sb.append((char)decimal);

			temp.append(decimal);
		}
		System.out.println("Decimal : " + temp.toString());

		return sb.toString();
	}
	public static String getFileExtension(String fn){
		if(fn==null || fn.isEmpty())
			return "";
		for(int i=fn.length()-1;i>=0;i--){
			if(fn.charAt(i)=='.')
				return fn.substring(i+1);
		}
		return "";
	}
	public static String getFileType(String fn){
		String extension=getFileExtension(fn);
		if(extension.toLowerCase().matches("png|jpeg|gif|bmp|jpg"))
			return "image/"+extension;
		else if(extension.toLowerCase().matches("mp3|mid|wav|m4a|mwav|ogg")){
			return "audio/"+extension;
		}else if(extension.toLowerCase().matches("mp4|flv|webm|3gp")){
			return "video/"+extension;
		}else if(extension.toLowerCase().matches("txt|pdf|xsl|ptpx|ptp|doc|docx")){
			return "document/"+extension;
		}
		else
			return "unknown";
	}
	public static String getLocalPath(String path) {
		return new File(Client.USERS_FILE_DATA_DIR+"/"+(new File(path).getName())).getAbsolutePath();
	}
	public static boolean existInLocal(String path) {
		// TODO Auto-generated method stub
		if(path==null)
			return false;
		return new File(Client.USERS_FILE_DATA_DIR+"/"+(new File(path).getName())).exists();
	}
}
