package objects;

import java.util.List;

import entity.User;

public class UserObject {
	
	private final int id;
	private final String firstName;
	private final String lastName;
	private final String email;
	private final String language;
	private final List<WorksheetObject> worksheetObjects;
	
	public UserObject(User user, List<WorksheetObject> worksheetObjects) {
		this.id = user.getId();
		this.firstName = user.getFirstName();
		this.lastName = user.getLastName();
		this.email = user.getEmail();
		this.language = user.getLanguage();
		this.worksheetObjects = worksheetObjects;
	}

	public int getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getEmail() {
		return email;
	}

	public String getLanguage() {
		return language;
	}
	
	public List<WorksheetObject> getWorksheetObjects() {
		return worksheetObjects;
	}
}
