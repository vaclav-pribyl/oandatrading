package wenaaa.oandatrading.logging;

import java.util.Arrays;

public class InfoBuilder {

	private static final int START_LENGTH = 20;
	private final StringBuilder sb;

	public InfoBuilder(final String start) {
		final int dif = START_LENGTH - start.length();
		sb = new StringBuilder(start);
		if (dif > 0) {
			final char[] spaces = new char[dif];
			Arrays.fill(spaces, 0, dif, ' ');
			sb.append(spaces);
		}
		sb.append(":");
	}

	public void append(final Object o, final boolean end) {
		sb.append(" ");
		sb.append(o);
		if (!end) {
			sb.append(" /");
		}
	}

	@Override
	public String toString() {
		return sb.toString();
	}
}
