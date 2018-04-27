package wenaaa.oandatrading.api;

import java.time.ZonedDateTime;

import com.oanda.v20.order.StopOrderRequest;
import com.oanda.v20.order.TimeInForce;
import com.oanda.v20.primitives.DateTime;

public class StopOrder implements Order {

	private double price;
	private String pair;
	private long units;
	private DateTime gtdTime;

	public void setPair(final FXPair pair) {
		this.pair = pair.getPair();
	}

	public void setUnits(final long units) {
		this.units = units;
	}

	public void setPrice(final double tradePrice) {
		price = tradePrice;
	}

	public void setExpiry(final ZonedDateTime expiry) {
		gtdTime = new DateTime(String.valueOf(expiry.toEpochSecond()));
	}

	@Override
	public double getPrice() {
		return price;
	}

	StopOrderRequest createStopOrderRequest() {
		final StopOrderRequest sor = new StopOrderRequest();
		sor.setTimeInForce(TimeInForce.GTD);
		sor.setInstrument(pair);
		sor.setPrice(getPriceString());
		sor.setUnits(units);
		sor.setGtdTime(gtdTime);
		return sor;
	}

	String getPriceString() {
		final String priceString = String.valueOf(price);
		final int point = priceString.indexOf('.');
		final int endIndex = Math.min(priceString.length(), point + getDisplayPrecission(pair) + 1);
		return priceString.substring(0, endIndex);
	}

	int getDisplayPrecission(final String pair) {
		return Account.getDisplayPrecision(pair);
	}

	@Override
	public String toString() {
		return "StopOrder > " + pair + " / " + price + " / " + units;
	}

}
