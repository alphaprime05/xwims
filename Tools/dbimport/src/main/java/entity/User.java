package entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


/**
 * The persistent class for the users database table.
 * 
 */
@Entity
@Table(name="users")
@NamedQueries({
	@NamedQuery(name="User.findAll", query="SELECT u FROM User u"),
	@NamedQuery(name="User.findById", query="SELECT u FROM User u WHERE u.id = :id"),
	@NamedQuery(name="User.findByEmail", query="SELECT u FROM User u WHERE u.email = :email"),
})
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id",unique=true, nullable = false)
	private Integer id;

	@Column(nullable=false)
	private String email;

	@Column(name="first_name", nullable = false)
	private String firstName;

	@Column(name="is_banned", nullable = false)
	private Boolean isBanned;

	@Column(name="is_certified", nullable = false)
	private Boolean isCertified;

	@Column(name="is_root", nullable = false)
	private Boolean isRoot;

	@Column(length=5, nullable = false)
	private String language;

	@Column(name="last_name", nullable = false)
	private String lastName;

	@Column(name="password_hash", nullable = false)
	private String passwordHash;
	
	@Column(name="password_salt", nullable = false)
	private String passwordSalt;

	@Column(name="registration_date", nullable = false)
	private Date registrationDate;
	
	@Column(name="is_registered", nullable = false)
	private Boolean isRegistered;

	@Column(name="random_identifier", nullable = false)
	private Integer randomIdentifier;
	
	public User() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public Boolean getIsBanned() {
		return this.isBanned;
	}

	public void setIsBanned(Boolean isBanned) {
		this.isBanned = isBanned;
	}

	public Boolean getIsCertified() {
		return this.isCertified;
	}

	public void setIsCertified(Boolean isCertified) {
		this.isCertified = isCertified;
	}

	public Boolean getIsRoot() {
		return this.isRoot;
	}

	public void setIsRoot(Boolean isRoot) {
		this.isRoot = isRoot;
	}

	public String getLanguage() {
		return this.language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPasswordHash() {
		return this.passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
	
	public String getPasswordSalt() {
		return this.passwordSalt;
	}

	public void setPasswordSalt(String passwordSalt) {
		this.passwordSalt = passwordSalt;
	}
	
	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public void setIsRegistered(Boolean isRegistered) {
		this.isRegistered = isRegistered;
	}
	
	public Boolean getIsRegistered() {
		return isRegistered;
	}
	
	public void setRandomIdentifier(Integer randomIdentifier) {
		this.randomIdentifier = randomIdentifier;
	}
	
	public int getRandomIdentifier() {
		return randomIdentifier;
	}

}