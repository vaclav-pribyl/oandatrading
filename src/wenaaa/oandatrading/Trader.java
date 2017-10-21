package wenaaa.oandatrading;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.ReentrantLock;

import com.oanda.fxtrade.api.API;
import com.oanda.fxtrade.api.Account;
import com.oanda.fxtrade.api.FXClient;
import com.oanda.fxtrade.api.InvalidPasswordException;
import com.oanda.fxtrade.api.InvalidUserException;
import com.oanda.fxtrade.api.MultiFactorAuthenticationException;
import com.oanda.fxtrade.api.RateTable;
import com.oanda.fxtrade.api.SessionException;
import com.oanda.fxtrade.api.User;

import wenaaa.loginutils.LoginData;

public class Trader implements Runnable, Observer {

	private final ReentrantLock tradeLock;
	private boolean stop = false;
	private FXClient fxClient;
	private final LoginData loginData;
	private User user;
	private Account account;
	private RateTable rateTable;

	public Trader(final LoginData ld, final ReentrantLock tradeLock) {
		this.tradeLock = tradeLock;
		this.loginData = ld;
	}

	@Override
	public void run() {
		try {
			tradeLock.lock();
			initOandaConnection();
			while (!stop) {
				trade();
			}
		} catch (final Exception e) {
			LoggingUtils.logException(e);
			if (e.getCause() instanceof InvalidUserException) {
				TradingApp.stop();
			}
		} finally {
			fxClient.logout();
			tradeLock.unlock();
		}
	}

	private void trade() {
		try {
			System.out.println("Trading...");
			Thread.sleep(1000);
		} catch (final InterruptedException e) {

		}
	}

	private void initOandaConnection() throws InvalidUserException, InvalidPasswordException, SessionException,
			MultiFactorAuthenticationException {
		System.out.println("Connecting...");
		fxClient = API.createFXTrade();
		fxClient.addObserver(this);
		fxClient.setProxy(false);
		fxClient.setWithRateThread(true);
		fxClient.setWithKeepAliveThread(true);
		fxClient.login(loginData.getLoginName(), loginData.getPassword());
	}

	public void stop() {
		stop = true;
	}

	@Override
	public void update(final Observable source, final Object status) {
		try {
			if (source == fxClient) {
				// connected to server, update User, Account and RateTable
				if (status.equals(FXClient.CONNECTED)) {
					System.out.println("Setting user...");
					user = fxClient.getUser();

					System.out.println("Setting account...");
					account = (Account) user.getAccounts().elementAt(0);

					System.out.println("Fetching rate table...");
					rateTable = fxClient.getRateTable();
				}
			}
		} catch (final SessionException e) {
			LoggingUtils.logException(e);
			stop();
		}
	}

}
