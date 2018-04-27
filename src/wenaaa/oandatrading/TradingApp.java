package wenaaa.oandatrading;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import wenaaa.loginutils.LoggingUtils;
import wenaaa.oandatrading.properties.PropertyManager;

/**
 * Entry point for trading application. Gets login data and runs
 * {@link Trader}s.
 *
 */
public class TradingApp {

	private static boolean stop = false;
	private static Trader activeTrader;
	private static final ReentrantLock TRADE_LOCK = new ReentrantLock();

	public static void main(final String[] args) throws IOException {
		try {
			System.err.close();

			loadSettings();

			startStopCondition();

			trade();
		} catch (final Exception e) {
			LoggingUtils.logException(e);
		} finally {
			cleanUp();
		}

	}

	public static void stop() {
		stop = true;
	}

	private static void loadSettings() {
		PropertyManager.loadSettings();
	}

	private static void cleanUp() {
		activeTrader.stop();
		LoggingUtils.stopLogging();
	}

	private static void trade() {

		while (!stop) {
			try {
				if (TRADE_LOCK.tryLock()) {
					runTrader();
				}
			} finally {
				if (TRADE_LOCK.isHeldByCurrentThread()) {
					TRADE_LOCK.unlock();
				}
			}
			try {
				Thread.sleep(1000);
			} catch (final InterruptedException e) {

			}
		}

	}

	private static void startStopCondition() {
		final Thread t = new Thread(() -> {
			try {
				System.in.read();
			} catch (final IOException e) {
				e.printStackTrace();
			} finally {
				stop = true;
			}
		});
		t.setDaemon(true);
		t.start();
	}

	private static void runTrader() {
		activeTrader = new Trader(TRADE_LOCK);
		new Thread(activeTrader).start();
	}

}
