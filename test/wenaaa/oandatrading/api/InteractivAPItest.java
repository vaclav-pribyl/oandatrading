package wenaaa.oandatrading.api;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import wenaaa.oandatrading.properties.PropertyManager;
import wenaaa.oandatrading.properties.TradedPair;

public class InteractivAPItest {

	@BeforeClass
	public static void setupClass() {
		PropertyManager.clearSettings();
		PropertyManager.loadSettings();
	}

	@AfterClass
	public static void cleanUp() {
		PropertyManager.clearSettings();
	}

	@Before
	public void setup() {
		System.out.println();
	}

	@After
	public void teardown() {
		System.out.println();
	}

	@Test
	public void testSummary() {
		System.out.println("SUMMARY TEST");
		for (final String accId : PropertyManager.getAccounts()) {
			final Account acc = API.createAccount(accId);
			System.out.println("Currency: " + acc.getHomeCurrency());
			System.out.println("Balance: " + acc.getBalance());
			System.out.println("Margin available: " + acc.getMarginAvailable());
			System.out.println("Margin used: " + acc.getMarginUsed());
			System.out.println("Position value: " + acc.getPositionValue());
			System.out.println("Unrealized PL: " + acc.getUnrealizedPL());
			System.out.println("Orders count: " + acc.getOrdersCount());
			for (final MarketOrder trade : acc.getTrades()) {
				System.out.println("OA trade: " + trade.getSummaryInfo());
			}
		}
	}

	@Test
	public void testRateCoef() {
		System.out.println("RATE COEF TEST");
		for (final String accountId : PropertyManager.getAccounts()) {
			for (final TradedPair tp : PropertyManager.getTradedPairs(accountId)) {
				final FXPair fxpair = API.createFXPair(accountId, tp.getName());
				try {
					System.out.println("Pair : " + fxpair.getPair());
					System.out.println("\task/bid : " + fxpair.getAsk() + " / " + fxpair.getBid());
					System.out.println("\trate coef : " + fxpair.getRateCoef(true));
				} catch (final Exception e) {
					System.out.println("Ex: " + e.getMessage());
				}
			}
		}
	}

	@Test
	public void testCandlePoint() {
		System.out.println("RATE CANDLE POINTS");
		for (final String accountId : PropertyManager.getAccounts()) {
			for (final TradedPair tp : PropertyManager.getTradedPairs(accountId)) {
				final FXPair fxpair = API.createFXPair(accountId, tp.getName());
				try {
					System.out.println("Pair : " + fxpair.getPair());
					final List<CandlePoint> candles = CandlePoint.get(fxpair, "M5", 3);
					for (final CandlePoint candle : candles) {
						System.out.println("\t" + candle);
					}
				} catch (final Exception e) {
					System.out.println("Ex: " + e.getMessage());
				}
			}
		}
	}
}
