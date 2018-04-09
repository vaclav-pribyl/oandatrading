package wenaaa.oandatrading.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSettingsParsing {

	@BeforeClass
	public static void setupClass() {
		final PropertiesHandler ph = new PropertiesHandler(new File("testsettings.xml"));
		ph.load();
	}

	@AfterClass
	public static void cleanUp() {
		PropertyManager.clearSettings();
	}

	@Test
	public void parseAccounts() {
		final Collection<String> accounts = PropertyManager.getAccounts();
		assertEquals(1, accounts.size());
		assertTrue(accounts.contains("1-3"));
	}

	@Test
	public void parseTradedPairs() {
		final Collection<TradedPair> tp = PropertyManager.getTradedPairs("1-3");
		assertEquals(3, tp.size());
		assertTrue(tp.contains(new TradedPair("AUD/USD", "short")));
		assertTrue(tp.contains(new TradedPair("AUD/CAD", "short")));
		assertTrue(tp.contains(new TradedPair("USD/CAD", "short")));
		assertTrue(tp.contains(new TradedPair("AUD/USD", "long")));
		assertTrue(tp.contains(new TradedPair("AUD/CAD", "long")));
		assertTrue(tp.contains(new TradedPair("USD/CAD", "long")));
	}

	@Test
	public void parsePosition() {
		final Collection<TradedPair> tp = PropertyManager.getTradedPairs("1-3");
		assertEquals(3, tp.size());
		for (final TradedPair pair : tp) {
			if ("AUD/USD".equals(pair.getName())) {
				assertEquals("long", pair.getPosition());
			}
			if ("AUD/CAD".equals(pair.getName())) {
				assertEquals("short", pair.getPosition());
			}
			if ("USD/CAD".equals(pair.getName())) {
				assertEquals("short", pair.getPosition());
			}
		}
	}

	@Test
	public void parseSLHandlingSettings() {
		final SLHandlingProperties slSettings = PropertyManager.getSLHandlingProperties();
		assertEquals(100, slSettings.getCandles());
		assertEquals(1.1, slSettings.getAddedSpaceCoef(), 1e-6);
		assertEquals("M5", slSettings.getTimeFrame());
	}

	@Test
	public void parseDistanceCoef() {
		assertEquals(12, PropertyManager.getDistanceKoef(), 1e-9);
	}

	@Test
	public void parseResetBalanceRatio() {
		assertEquals(4.3, PropertyManager.getResetBalanceRatio(), 1e-9);
	}

	@Test
	public void parseRiskCoef() {
		assertEquals(3.6, PropertyManager.getRiskCoef(), 1e-9);
	}

	@Test
	public void parseMinProfitCoef() {
		assertEquals(1.2, PropertyManager.getMinProfitCoef(), 1e-9);
	}

	@Test
	public void parseLoginToken() {
		assertEquals("12a3f", PropertyManager.getLoginToken());
	}
}
