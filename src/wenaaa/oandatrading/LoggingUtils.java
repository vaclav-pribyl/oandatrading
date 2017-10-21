package wenaaa.oandatrading;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class LoggingUtils {

	private static final LoggingUtils instance = new LoggingUtils();
	private final PrintWriter errWriter;

	private LoggingUtils() {
		final File errFile = new File("logs/err.log");
		try {
			errWriter = new PrintWriter(errFile);
		} catch (final FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static void logException(final Throwable t) {
		t.printStackTrace(instance.errWriter);
	}

	public static void stopLogging() {
		instance.dispose();
	}

	private void dispose() {
		errWriter.flush();
		errWriter.close();
	}
}
