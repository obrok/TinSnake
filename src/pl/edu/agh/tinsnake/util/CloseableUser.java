package pl.edu.agh.tinsnake.util;

import java.io.Closeable;
import java.io.IOException;

public interface CloseableUser {
	public void performAction(Closeable stream) throws IOException;
}
