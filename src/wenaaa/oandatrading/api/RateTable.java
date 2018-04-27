package wenaaa.oandatrading.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.pricing.Price;
import com.oanda.v20.pricing.PriceBucket;
import com.oanda.v20.pricing.PricingGetResponse;

import wenaaa.oandatrading.properties.PropertyManager;
import wenaaa.oandatrading.properties.TradedPair;

public class RateTable {
	private PricingGetResponse detail;
	private long detailTimeStamp;
	private final String accountID;

	RateTable(final String accID) {
		accountID = accID;
	}

	public FXTick getRate(final FXPair pair) {
		return createFxTick(getPrice(pair));
	}

	private Price getPrice(final FXPair pair) {
		final List<Price> prices = getDetail().getPrices();
		for (final Price price : prices) {
			if (price.getInstrument().equals(pair.getPair())) {
				return price;
			}
		}
		return null;
	}

	private FXTick createFxTick(final Price price) {
		double a = Double.MAX_VALUE;
		for (final PriceBucket ask : price.getAsks()) {
			final double val = ask.getPrice().doubleValue();
			if (val < a) {
				a = val;
			}
		}
		double b = -1;
		for (final PriceBucket bid : price.getBids()) {
			final double val = bid.getPrice().doubleValue();
			if (val > b) {
				b = val;
			}
		}
		return new FXTick(a, b);
	}

	public List<CandlePoint> getCandles(final FXPair pair, final String timeFrame, final int candles) {
		return CandlePoint.get(pair, timeFrame, candles);
	}

	private synchronized PricingGetResponse getDetail() {
		if (detail == null || isOld()) {
			try {
				final Collection<TradedPair> tps = PropertyManager.getTradedPairs(accountID);
				final Set<String> instruments = new HashSet<>();
				for (final TradedPair tp : tps) {
					instruments.add(tp.getName());
				}
				detail = API.getContext().pricing.get(new AccountID(accountID), instruments);
				detailTimeStamp = System.currentTimeMillis();
			} catch (RequestException | ExecuteException e) {
				throw new TradeApiException(e);
			}
		}
		return detail;
	}

	private boolean isOld() {
		return (System.currentTimeMillis() - detailTimeStamp) > 1000;
	}

}
