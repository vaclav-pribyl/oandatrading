package wenaaa.oandatrading;

import com.oanda.fxtrade.api.Account;
import com.oanda.fxtrade.api.Order;
import com.oanda.fxtrade.api.RateTable;

import wenaaa.oandatrading.properties.PropertyManager;
import wenaaa.oandatrading.properties.TradedPair;

public class OrdersPoster {

	private String pair;
	private boolean buyPair;
	private RateTable rateTable;
	private Account account;
	private double distance_koef;

	public OrdersPoster(TradedPair pair, RateTable rateTable, Account acc) {
		this.pair = pair.getName();
		this.buyPair = pair.isBuyPair();
		this.rateTable = rateTable;
		account = acc;
		distance_koef = PropertyManager.getDistanceKoef();
	}

	public void trade() {
		
	}
	
	double getDistanceKoef(){
		return distance_koef;
	}

	double getAsk(){
		return rateTable.getInstrument(pair).getAsk();
	}
	
	double getBid(){
		return rateTable.getInstrument(pair).getBid();
	}
	
	boolean isConflictingTrade(Order trade){
		double ask = getAsk();
		double bid = getBid();
		double distance = getDistanceKoef()*(ask-bid);
		double tradePrice = trade.getPrice();
		if(isBuyPair()){
			return  tradePrice > ask && tradePrice < ask + 2 * distance; 
		}
		return tradePrice < bid && tradePrice > bid - 2 * distance;		
	}

	boolean isBuyPair() {
		return buyPair;
	}
}
