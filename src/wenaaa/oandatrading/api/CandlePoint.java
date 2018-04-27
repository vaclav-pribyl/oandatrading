package wenaaa.oandatrading.api;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.instrument.Candlestick;
import com.oanda.v20.instrument.CandlestickData;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.instrument.InstrumentCandlesResponse;
import com.oanda.v20.primitives.InstrumentName;

public class CandlePoint {

	private static final Map<String, List<CandlePoint>> cache = new HashMap<>();
	private final ZonedDateTime time;
	private final double close;
	private final double min;
	private final double max;

	private CandlePoint(final Candlestick candle) {
		time = ZonedDateTime.parse(candle.getTime());
		final CandlestickData mid = candle.getMid();
		close = mid.getC().doubleValue();
		min = mid.getL().doubleValue();
		max = mid.getH().doubleValue();
	}

	@Override
	public String toString() {
		return time + " / " + max + " / " + min + " / " + close;
	}

	public ZonedDateTime getTime() {
		return time;
	}

	public double getClose() {
		return close;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	public static List<CandlePoint> get(final FXPair pair, final String timeFrame, final int candles) {
		if (shouldLoad(pair, timeFrame, candles)) {
			load(pair, timeFrame, candles);
		}
		return cache.get(pair + timeFrame);
	}

	private static boolean shouldLoad(final FXPair pair, final String timeFrame, final int candles) {
		final List<CandlePoint> cached = cache.get(pair + timeFrame);
		if (cached == null || cached.size() < candles) {
			return true;
		}
		final CandlePoint last = cached.get(cached.size() - 1);
		return last.time.plus(getAmount(timeFrame), getTempUnit(timeFrame)).isBefore(ZonedDateTime.now());
	}

	private static TemporalUnit getTempUnit(final String timeFrame) {
		if ("M".equals(timeFrame)) {
			return ChronoUnit.MONTHS;
		}
		if (timeFrame.startsWith("W")) {
			return ChronoUnit.WEEKS;
		}
		if (timeFrame.startsWith("D")) {
			return ChronoUnit.DAYS;
		}
		if (timeFrame.startsWith("H")) {
			return ChronoUnit.HOURS;
		}
		if (timeFrame.startsWith("M")) {
			return ChronoUnit.MINUTES;
		}
		if (timeFrame.startsWith("S")) {
			return ChronoUnit.SECONDS;
		}
		throw new TradeApiException("Unknown time frame.");
	}

	private static long getAmount(final String timeFrame) {
		if (timeFrame.length() == 1) {
			return 1;
		}
		return Integer.valueOf(timeFrame.substring(1));
	}

	private static void load(final FXPair pair, final String timeFrame, final int candles) {
		final InstrumentCandlesRequest request = new InstrumentCandlesRequest(new InstrumentName(pair.getPair()));
		request.setCount(candles);
		request.setGranularity(CandlestickGranularity.valueOf(timeFrame));
		try {
			final InstrumentCandlesResponse response = API.getContext().instrument.candles(request);
			final List<CandlePoint> candlePoints = new ArrayList<>();
			for (final Candlestick candle : response.getCandles()) {
				candlePoints.add(new CandlePoint(candle));
			}
			cache.put(pair + timeFrame, candlePoints);
		} catch (RequestException | ExecuteException e) {
			throw new TradeApiException(e);
		}

	}

}
