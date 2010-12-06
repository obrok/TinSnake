package pl.edu.agh.tinsnake.util;

import java.io.IOException;
import java.io.OutputStream;


public class StreamUtil {
	public static void safelyAcccess(OutputStream stream, OutputStreamUser user) throws IOException{
		try{
			user.performAction(stream);			
		} finally {
			try{
				stream.close();
			}catch(Exception e){
				//ignore exception during cleanup
			}
		}
	}
}
