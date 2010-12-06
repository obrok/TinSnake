package pl.edu.agh.tinsnake.util;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;


public class StreamUtil {
	public static void safelyAcccess(Closeable stream, CloseableUser user) throws IOException{
		try{
			user.performAction(stream);
			if(stream instanceof Flushable){
				((Flushable)stream).flush();
			}
		} finally {
			try{
				stream.close();
			}catch(Exception e){
				//ignore exception during cleanup
			}
		}
	}
}
