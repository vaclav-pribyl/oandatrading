package wenaaa.oandatrading;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import wenaaa.loginutils.ConsoleLogin;
import wenaaa.loginutils.LoginData;
import wenaaa.loginutils.NoConsoleException;

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

		final LoginData ld = getLoginData(args);
		if (ld == null) {
			return;
		}

		startStopCondition();

		trade(ld);

		cleanUp();
	}

	private static void cleanUp() {
		activeTrader.stop();
	}

	private static void trade(final LoginData ld) {
		while (!stop) {
			try {
				if (TRADE_LOCK.tryLock()) {
					runTrader(ld);
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
		new Thread(() -> {
			try {
				System.in.read();
			} catch (final IOException e) {
				e.printStackTrace();
			} finally {
				stop = true;
			}
		}).start();
	}

	private static LoginData getLoginData(final String[] args) {
		LoginData ld;
		try {
			ld = ConsoleLogin.getLoginData();
		} catch (final NoConsoleException e) {
			if (args.length == 2) {
				ld = new LoginData(args[0], args[1]);
			} else {
				ld = null;
			}
		}
		return ld;
	}

	private static void runTrader(final LoginData ld) {
		activeTrader = new Trader(ld, TRADE_LOCK);
		new Thread(activeTrader).start();
	}

}
