package wenaaa.loginutils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

public class LoggingUtils {

	private static LoggingUtils instance = new LoggingUtils();
	private PrintStream infoStream;
	private final String errFileName;

	protected LoggingUtils() {
		errFileName = "logs/err" + (System.currentTimeMillis() / 1000) + ".log";
		infoStream = System.out;
	}

	public static void setLogger(final LoggingUtils logger) {
		instance = logger;
	}

	public static void logException(final Throwable t) {
		instance.doLogException(t);
	}

	protected void doLogException(final Throwable t) {
		try {
			final PrintWriter pw = new PrintWriter(new FileWriter(errFileName, true));
			t.printStackTrace(pw);
			pw.flush();
			pw.close();
		} catch (final IOException e) {
			e.printStackTrace(infoStream);
		}

	}

	public static void stopLogging() {
		instance.dispose();
		instance = null;
	}

	protected void dispose() {
		infoStream = null;
	}

	public static void logInfo(final String message) {
		instance.doLogInfo(message);
	}

	protected void doLogInfo(final String message) {
		infoStream.println(message);
	}
}
