package wenaaa.oandatrading;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.oanda.fxtrade.api.API;
import com.oanda.fxtrade.api.Account;
import com.oanda.fxtrade.api.AccountException;
import com.oanda.fxtrade.api.CandlePoint;
import com.oanda.fxtrade.api.FXPair;
import com.oanda.fxtrade.api.Instrument;
import com.oanda.fxtrade.api.MarketOrder;
import com.oanda.fxtrade.api.OAException;
import com.oanda.fxtrade.api.RateTable;
import com.oanda.fxtrade.api.StopLossOrder;

import wenaaa.loginutils.LoggingUtils;
import wenaaa.oandatrading.properties.PropertyManager;
import wenaaa.oandatrading.properties.SLHandlingProperties;
import wenaaa.oandatrading.properties.TradedPair;

public class StopLossHandler {

	private final String pair;
	private final boolean buyPair;
	private final SLHandlingProperties slHandling;
	private final RateTable rateTable;
	private Account account;

	public StopLossHandler(final TradedPair pair, final RateTable rateTable, Account acc) {
		this.pair = pair.getName();
		this.buyPair = pair.isBuyPair();
		slHandling = PropertyManager.getSLHandlingProperties();
		this.rateTable = rateTable;
		account = acc;
	}

	public void handleSL() {
		double price;
		try {
			price = getPrice();
			if (price == 0) {
				return;
			}
		} catch (final OAException e) {
			log(e);
			return;
		}
		applySL(price);
		return;
	}

	void applySL(double price) {
		List<? extends MarketOrder> ot = getOpenTrades();
		StopLossOrder slOrder = getSLOrder(price);
		for(MarketOrder trade : ot){
			if(isAcceptable(price,trade)){
				try
                {
					LoggingUtils.logInfo("SSL > " + trade);
					trade.setStopLoss(slOrder);
                    getAcc().modify(trade);
                    LoggingUtils.logInfo("OK");
                }
                catch (final OAException e)
                {
                    LoggingUtils.logInfo("Can't set SL > "+e.getMessage());
                }
			}
		}
		
	}

	Account getAcc() {
		return account;
	}

	List<MarketOrder> getOpenTrades() {
		Vector trades;
		try {
			trades = getAcc().getTrades();
		} catch (AccountException e) {
			LoggingUtils.logException(e);
			LoggingUtils.logInfo("Can't get trade list: "+e.getMessage());
			return null;
		}
		List<MarketOrder> list = (List<MarketOrder>) trades.stream().filter(new Predicate<MarketOrder>()
        {

            @Override
            public boolean test(final MarketOrder t)
            {
                return t.getPair().getPair().equals(pair);
            }
        }).collect(Collectors.toList());
		return list;
	}

	void log(final OAException e) {
		LoggingUtils.logInfo("Can not set SL: " + e.getMessage());
		LoggingUtils.logException(e);
	}

	boolean isAcceptable(double price, MarketOrder trade) {
		if (isBuyPair())
        {
            return price > trade.getPrice() && price > trade.getStopLoss().getPrice();
        }
        if (trade.getStopLoss().getPrice() != 0)
        {
            return price < trade.getPrice() && price < trade.getStopLoss().getPrice();
        }
        return price < trade.getPrice();
	}

	StopLossOrder getSLOrder(double price) {
		return API.createStopLossOrder(price);
	}

	double getPrice() throws OAException {
		double answ = 0;
		final List<CandlePoint> cpl = getcandles();
		final CandlePoint lcc = cpl.get(cpl.size() - 2); // last closed candle
		final double refClose = lcc.getClose();
		double extrem = isBuyPair() ? lcc.getMin() : lcc.getMax();
		final double refextrem = extrem;
		for (int i = cpl.size() - 3; i >= 0; i--) {
			final CandlePoint cp = cpl.get(i);
			if (isBuyPair() && cp.getMin() < extrem) {
				extrem = cp.getMin();
			}
			if (!isBuyPair() && cp.getMax() > extrem) {
				extrem = cp.getMax();
			}
			if (isBuyPair() && cp.getMax() < refClose && cp.getMin() < refextrem) {
				answ = extrem - getSLspace();
				break;
			}
			if (!isBuyPair() && cp.getMin() > refClose && cp.getMax() > refextrem) {
				answ = extrem + getSLspace();
				break;
			}
		}
		return answ;
	}

	boolean isBuyPair() {
		return buyPair;
	}

	double getSLspace() {
		final Instrument instrument = rateTable.getInstrument(pair);
		final double ask = instrument.getAsk();
		final double bid = instrument.getBid();
		return (ask - bid) * slHandling.getAddedSpaceCoef();
	}

	List<CandlePoint> getcandles() throws OAException {
		return new ArrayList<>(
				rateTable.getCandles(getAPIPair(), slHandling.getTimeFrameValue(), slHandling.getCandles()));
	}

	private FXPair getAPIPair() {
		return API.createFXPair(pair);
	}
}
