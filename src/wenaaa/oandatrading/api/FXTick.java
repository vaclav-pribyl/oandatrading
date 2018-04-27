package wenaaa.oandatrading.api;

public class FXTick {

	private final double ask;
	private final double bid;

	public FXTick(final double a, final double b) {
		ask = a;
		bid = b;
	}

	public double getAsk() {
		return ask;
	}

	public double getBid() {
		return bid;
	}

}
