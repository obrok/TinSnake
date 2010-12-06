package pl.edu.agh.tinsnake.util;

import java.io.IOException;
import java.io.OutputStream;

public interface OutputStreamUser {
	public void performAction(OutputStream stream) throws IOException;
}
