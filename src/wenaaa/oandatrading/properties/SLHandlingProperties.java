package wenaaa.oandatrading.properties;

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

}
