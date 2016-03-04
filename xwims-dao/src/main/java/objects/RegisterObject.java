package objects;


public class RegisterObject {
	
	private String email;
	private String password;
	private String first_name;
	private String last_name;
	private String lang;

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}

	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public String getFirst_name() {
		return first_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public String getLang() {
		return lang;
	}

	@Override
	public String toString() {
		return "RegisterObject [email=" + email + ", password=" + password + ", first_name=" + first_name
				+ ", last_name=" + last_name + ", lang=" + lang + "]";
	}
}
