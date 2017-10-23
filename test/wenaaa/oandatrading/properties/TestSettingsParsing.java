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
		final Collection<Integer> accounts = PropertyManager.getAccounts();
		assertEquals(1, accounts.size());
		assertTrue(accounts.contains(13));
	}

	@Test
	public void parseTradedPairs() {
		final Collection<TradedPair> tp = PropertyManager.getTradedPairs(13);
		assertEquals(3, tp.size());
		assertTrue(tp.contains(new TradedPair("AUD/USD", "")));
		assertTrue(tp.contains(new TradedPair("AUD/CAD", "")));
		assertTrue(tp.contains(new TradedPair("USD/CAD", "")));
	}

	@Test
	public void parsePosition() {
		final Collection<TradedPair> tp = PropertyManager.getTradedPairs(13);
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
}
