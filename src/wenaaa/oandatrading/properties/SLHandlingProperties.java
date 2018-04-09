package wenaaa.oandatrading.properties;

import wenaaa.oandatrading.api.API;

public class SLHandlingProperties {

	private final int candles;
	private final float addedSpaceCoef;
	private final String timeFrame;

	public SLHandlingProperties(final int candles, final float addedSpaceCoef, final String timeFrame) {
		this.candles = candles;
		this.addedSpaceCoef = addedSpaceCoef;
		this.timeFrame = timeFrame;
	}

	public int getCandles() {
		return candles;
	}

	public float getAddedSpaceCoef() {
		return addedSpaceCoef;
	}

	public String getTimeFrame() {
		return timeFrame;
	}

	public long getTimeFrameValue() {
		final long koef = getKoef();
		return koef * Integer.parseInt(timeFrame.substring(1));
	}

	private long getKoef() {
		if (timeFrame.startsWith("M")) {
			return API.INTERVAL_1_MIN;
		}
		if (timeFrame.startsWith("H")) {
			return API.INTERVAL_1_HOUR;
		}
		if (timeFrame.startsWith("D")) {
			return API.INTERVAL_1_DAY;
		}
		return -1;
	}
}
