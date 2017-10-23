package wenaaa.oandatrading.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.junit.Test;

public class TestPropertyManager {

	@Test
	public void testAccount() {
		PropertyManager.addAccount(1);
		PropertyManager.addAccount(2);
		Collection<Integer> accounts = PropertyManager.getAccounts();
		assertEquals(2, accounts.size());
		PropertyManager.addAccount(1);
		accounts = PropertyManager.getAccounts();
		assertEquals(2, accounts.size());
		assertTrue(accounts.contains(2));
		assertTrue(accounts.contains(2));
		PropertyManager.clearSettings();
		accounts = PropertyManager.getAccounts();
		assertEquals(0, accounts.size());
	}

	@Test
	public void testTradedPAirs() {
		PropertyManager.addAccount(1);
		PropertyManager.addTradedPair(1, "pair1", "long");
		PropertyManager.addTradedPair(1, "pair2", "short");
		Collection<TradedPair> pairs = PropertyManager.getTradedPairs(1);
		assertEquals(2, pairs.size());
		try {
			pairs.add(new TradedPair("pair3", "long"));
			fail();
		} catch (final UnsupportedOperationException e) {
			// nothing to do
		}
		PropertyManager.clearSettings();
		pairs = PropertyManager.getTradedPairs(1);
		assertEquals(0, pairs.size());
	}

}
