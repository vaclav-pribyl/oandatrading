package wenaaa.loginutils;

import java.io.Console;

public class ConsoleLogin {

	private ConsoleLogin() {

	}

	public static LoginData getLoginData() throws NoConsoleException {
		final Console console = System.console();
		if (console == null) {
			throw new NoConsoleException();
		}
		final String name = console.readLine("%s", "login name:");
		final char[] pass = console.readPassword("%s", "password:");

		return new LoginData(name, String.copyValueOf(pass));
	}
}
