package wenaaa.oandatrading;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.ReentrantLock;

import wenaaa.loginutils.LoggingUtils;
import wenaaa.loginutils.LoginData;
import wenaaa.oandatrading.api.API;
import wenaaa.oandatrading.api.Account;
import wenaaa.oandatrading.api.FXClient;
import wenaaa.oandatrading.api.RateTable;
import wenaaa.oandatrading.api.User;
import wenaaa.oandatrading.logging.InfoBuilder;
import wenaaa.oandatrading.properties.PropertyManager;
import wenaaa.oandatrading.properties.TradedPair;

public class Trader implements Runnable, Observer {

	private static final String LAST_BALANCE_PATH = "lastBalance";
	private static final String REPORT_PATH = "results";
	private final ReentrantLock tradeLock;
	private boolean stop = false;
	private FXClient fxClient;
	private final LoginData loginData;
	private User user;
	private RateTable rateTable;
	private final Collection<Account> accounts;
	private final Map<Account, Collection<TradedPair>> tradedPairs;
	private double lastBalance;
	private int infoCounter;
	private LocalDateTime reportTime;

	public Trader(final LoginData ld, final ReentrantLock tradeLock) {
		this.tradeLock = tradeLock;
		this.loginData = ld;
		accounts = new ArrayList<>();
		tradedPairs = new HashMap<>();
		loadLastBalance();
		setReportTime();
	}

	protected void setReportTime() {
		final File reportFile = new File(REPORT_PATH);
		try {
			final BufferedReader br = new BufferedReader(new FileReader(reportFile));
			initReportTime(br);
			br.close();
		} catch (final IOException e) {
			logAndThrowRuntime(e, "Can't read " + REPORT_PATH + " file.");
		}
	}

	void initReportTime(final BufferedReader br) throws IOException {
		boolean todayReported = false;
		String line;
		final String date = LocalDate.now().toString();
		while ((line = br.readLine()) != null) {
			if (line.startsWith(date)) {
				todayReported = true;
				break;
			}
		}
		if (todayReported) {
			resetReportTime();
		} else {
			reportTime = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).withHour(9);
		}
	}

	protected void loadLastBalance() {
		final File lbFile = new File(LAST_BALANCE_PATH);
		try {
			final BufferedReader br = new BufferedReader(new FileReader(lbFile));
			lastBalance = Double.parseDouble(br.readLine());
			br.close();
		} catch (final NumberFormatException e) {
			logAndThrowRuntime(e, "Wrong number format in lastBalance file.");
		} catch (final IOException e) {
			logAndThrowRuntime(e, "Can't read " + LAST_BALANCE_PATH + " file.");
		}
	}

	private void logAndThrowRuntime(final Throwable e, final String message) {
		LoggingUtils.logException(e);
		LoggingUtils.logInfo(message);
		throw new RuntimeException(message);
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
			LoggingUtils.logInfo("Exception: " + e.getMessage());
			TradingApp.stop();
		} finally {
			fxClient.logout();
			tradeLock.unlock();
		}
	}

	protected void trade() {
		try {
			if (infoCounter == 60) {
				printInfo();
				lastBalanceReset();
				infoCounter = 0;
				if (LocalDateTime.now().isAfter(getReportTime())) {
					report();
					resetReportTime();
				}
			} else {
				handleSL();
				postOrders();
				infoCounter++;
			}
			sleep(1000);
		} catch (final InterruptedException e) {

		}
	}

	void sleep(final long time) throws InterruptedException {
		Thread.sleep(time);
	}

	LocalDateTime getReportTime() {
		return reportTime;
	}

	void resetReportTime() {
		reportTime = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).plus(1, ChronoUnit.DAYS).withHour(9);
	}

	protected void report() {
		final File reportFile = new File(REPORT_PATH);
		try (FileWriter fw = new FileWriter(reportFile, true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw)) {
			reportResults(pw);
		} catch (final IOException ex) {
			LoggingUtils.logException(ex);
			LoggingUtils.logInfo("Can't write report to file (" + REPORT_PATH + "): " + ex.getMessage());
		}
	}

	void reportResults(final PrintWriter pw) {
		final LocalDate date = LocalDate.now();
		final StringBuilder sb = new StringBuilder(date.toString() + " ");
		sb.append(getTotalBalance() + " ");
		sb.append((getTotalBalance() + getTotalUPL()) + " ");
		sb.append(getTotalTrades() + " ");
		sb.append(getTotalOrders());
		pw.println(sb.toString());
	}

	int getTotalOrders() {
		int orders = 0;
		for (final Account acc : getAccounts()) {
			orders += acc.getOrders().size();
		}
		return orders;
	}

	double getTotalUPL() {
		double upl = 0.0;
		for (final Account acc : getAccounts()) {
			upl += acc.getUnrealizedPL();
		}
		return upl;
	}

	int getTotalTrades() {
		int trades = 0;
		for (final Account acc : getAccounts()) {
			trades += acc.getTrades().size();
		}
		return trades;
	}

	protected void lastBalanceReset() {
		final double nav = getTotalBalance() + getTotalUPL();
		if (nav > getBalanceReset()) {
			closeTrades();
			updateLastBalance();
		}
	}

	double getBalanceReset() {
		return getLastBalance() * PropertyManager.getResetBalanceRatio();
	}

	double getLastBalance() {
		return lastBalance;
	}

	void closeTrades() {
		new TradesCloser(accounts, rateTable).closeTrades();
	}

	double getTotalBalance() {
		double balance = 0.0;
		for (final Account acc : getAccounts()) {
			balance += acc.getBalance();
		}
		return balance;
	}

	Collection<Account> getAccounts() {
		return accounts;
	}

	protected void postOrders() {
		for (final Account acc : getAccounts()) {
			for (final TradedPair pair : tradedPairs.get(acc)) {
				new OrdersPoster(pair, rateTable, acc).trade();
			}
		}
	}

	protected void handleSL() {
		for (final Account acc : getAccounts()) {
			for (final TradedPair pair : tradedPairs.get(acc)) {
				new StopLossHandler(pair, rateTable, acc).handleSL();
			}
		}
	}

	protected void printInfo() {
		final InfoBuilder timeIB = new InfoBuilder("Accounts data for");
		timeIB.append(new Date(), true);
		LoggingUtils.logInfo(timeIB.toString());
		final InfoBuilder balanceIB = new InfoBuilder("Balance");
		double balance = 0.0;
		final InfoBuilder uplIB = new InfoBuilder("Unrealized P/L");
		double uPotLoss = 0.0;
		final InfoBuilder navIB = new InfoBuilder("Net Asset Value");
		double nav = 0.0;
		final InfoBuilder muIB = new InfoBuilder("Margin Used");
		final InfoBuilder maIB = new InfoBuilder("Margin Available");
		final InfoBuilder pvIB = new InfoBuilder("Position Value");
		double posValue = 0.0;
		final InfoBuilder tIB = new InfoBuilder("Trades");
		int trades = 0;
		final InfoBuilder oIB = new InfoBuilder("Orders");
		int orders = 0;
		final Iterator<Account> iter = getAccounts().iterator();
		while (iter.hasNext()) {
			final Account acc = iter.next();
			final boolean last = !iter.hasNext();
			final double bal = acc.getBalance();
			balance += bal;
			balanceIB.append(bal, false);
			final double upl = acc.getUnrealizedPL();
			uPotLoss += upl;
			uplIB.append(upl, false);
			nav += bal + upl;
			navIB.append(bal + upl, false);
			muIB.append(acc.getMarginUsed(), last);
			maIB.append(acc.getMarginAvailable(), last);
			final double pv = acc.getPositionValue();
			posValue += pv;
			pvIB.append(pv, false);
			final int tr = acc.getTrades().size();
			trades += tr;
			tIB.append(tr, false);
			final int o = acc.getOrders().size();
			orders += o;
			oIB.append(o, false);
		}
		balanceIB.append(balance, true);
		LoggingUtils.logInfo(balanceIB.toString());
		uplIB.append(uPotLoss, true);
		LoggingUtils.logInfo(uplIB.toString());
		navIB.append(nav, true);
		LoggingUtils.logInfo(navIB.toString());
		LoggingUtils.logInfo(muIB.toString());
		LoggingUtils.logInfo(maIB.toString());
		pvIB.append(posValue, true);
		LoggingUtils.logInfo(pvIB.toString());
		tIB.append(trades, true);
		LoggingUtils.logInfo(tIB.toString());
		oIB.append(orders, true);
		LoggingUtils.logInfo(oIB.toString());
		final InfoBuilder rbIB = new InfoBuilder("Next Balance Reset");
		rbIB.append(getBalanceReset(), true);
		LoggingUtils.logInfo(rbIB.toString());
	}

	protected void initOandaConnection() {
		LoggingUtils.logInfo("Connecting...");
		fxClient = API.createFXTrade();
		// fxClient.addObserver(this);
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
		/*
		 * try { if (source == fxClient && status.equals(FXClient.CONNECTED)) {
		 * setUser(); setAccounts(); if (getLastBalance() < 0) { updateLastBalance(); }
		 * setRateTable(); } } catch (final SessionException e) {
		 * LoggingUtils.logException(e); stop(); } catch (final AccountException e) {
		 * LoggingUtils.logException(e); stop(); LoggingUtils.logInfo(e.getMessage());
		 * TradingApp.stop(); }
		 */
	}

	void updateLastBalance() {
		lastBalance = getTotalBalance();
		storeLastBalance();
	}

	void storeLastBalance() {
		final File balaceFile = new File(LAST_BALANCE_PATH);
		try (FileWriter fw = new FileWriter(balaceFile);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw)) {
			pw.println(getLastBalance());
		} catch (final IOException e) {
			LoggingUtils.logException(e);
			LoggingUtils.logInfo("Can't write lastBalance file (" + getLastBalance() + "): " + e.getMessage());
		}
		final InfoBuilder ib = new InfoBuilder("Last balance set");
		ib.append(getLastBalance(), true);
		LoggingUtils.logInfo(ib.toString());
	}

	void setRateTable() {
		LoggingUtils.logInfo("Fetching rate table...");
		rateTable = fxClient.getRateTable();
	}

	void setAccounts() {
		LoggingUtils.logInfo("Setting account...");
		for (final String acc_id : PropertyManager.getAccounts()) {
			final Account acc = user.getAccountWithId(acc_id);
			getAccounts().add(acc);
			final InfoBuilder ib = new InfoBuilder("Using account");
			ib.append(acc_id, true);
			LoggingUtils.logInfo(ib.toString());
			tradedPairs.put(acc, PropertyManager.getTradedPairs(acc_id));
		}
	}

	void setUser() {
		LoggingUtils.logInfo("Setting user...");
		user = fxClient.getUser();
	}

}
