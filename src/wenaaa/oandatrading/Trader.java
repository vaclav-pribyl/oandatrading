package wenaaa.oandatrading;

import java.util.concurrent.locks.ReentrantLock;

import wenaaa.loginutils.LoginData;

public class Trader implements Runnable {

	private final ReentrantLock tradeLock;
	private boolean stop = false;

	public Trader(final LoginData ld, final ReentrantLock tradeLock) {
		this.tradeLock = tradeLock;
	}

	@Override
	public void run() {
		try {
			tradeLock.lock();
			while (!stop) {
				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}
		} finally {
			tradeLock.unlock();
		}
	}

	public void stop() {
		stop = true;
	}

}
