package wenaaa.loginutils;

public class LoginData {
	private final String loginName;
	private final String password;

	public LoginData(final String loginName, final String password) {
		this.loginName = loginName;
		this.password = password;
	}

	public String getLoginName() {
		return loginName;
	}

	public String getPassword() {
		return password;
	}
}
