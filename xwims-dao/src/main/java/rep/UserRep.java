package rep;

import java.util.Date;

import entity.User;

public class UserRep {
	private final Integer id;
	private final String email;
	private final String firstName;
	private final Boolean isBanned;
	private final Boolean isCertified;
	private final Boolean isRoot;
	private final Boolean isRegistered;
	private final String language;
	private final String lastName;
	private final String passwordHash;
	private final String passwordSalt;
	private final Date registrationDate;
	
	public UserRep(User u) {
		id = u.getId();
		email = u.getEmail();
		firstName = u.getFirstName();
		lastName = u.getLastName();
		isBanned = u.getIsBanned();
		isCertified = u.getIsCertified();
		isRoot = u.getIsRoot();
		language = u.getLanguage();
		passwordHash = u.getPasswordHash();
		passwordSalt = u.getPasswordSalt();
		registrationDate = u.getRegistrationDate();
		isRegistered = u.getIsRegistered();
	}

	public Boolean getIsRegistered() {
		return isRegistered;
	}

	public Integer getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstName() {
		return firstName;
	}

	public Boolean getIsBanned() {
		return isBanned;
	}

	public Boolean getIsCertified() {
		return isCertified;
	}

	public Boolean getIsRoot() {
		return isRoot;
	}

	public String getLanguage() {
		return language;
	}

	public String getLastName() {
		return lastName;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getPasswordSalt() {
		return passwordSalt;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}
}
