package dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import entity.AuthorizedDomainName;
import entity.User;
import exception.DAOException;
import objects.UserObject;
import objects.WorksheetObject;
import rep.UserRep;
import security.HashedPassword;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class UserDao extends AbstractGenericDao<User, String> {

	@Inject
	private ExercisesCategoryVoteDao ecv;

	@Inject
	private ExercisesKeywordsVoteDao ekv;

	@Inject
	private ExercisesLevelsVoteDao elv;
	
	@Inject
	private WorksheetDao worksheetDao;
	
	@Resource
	private UserTransaction utx;

	public UserDao() {
		super(User.class);
	}

	/**
	 * Get an user by its ID
	 * @param id The id of the user
	 * @return The selected user
	 * @throws DAOException If the User ID is invalid
	 */
	public User getUserById(int id) throws DAOException {
		try{
			User u = em.createNamedQuery("User.findById", User.class)
					.setParameter("id", id)
					.getSingleResult();
			return u;
		}catch(NoResultException e){
			throw new DAOException("8");
		}catch(NonUniqueResultException e){
			throw new DAOException("13");
		}catch(IllegalStateException | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}
	
	/**
	 * Get an user representation by its ID
	 * @param id The id of the user
	 * @return The representation of the selected user
	 * @throws DAOException If the User ID is invalid
	 */
	public UserRep getUserRepById(int id) throws DAOException {
		return new UserRep(getUserById(id));
	}

	/**
	 * Get a representation of all the users in the database
	 * @return The list of all the users
	 */
	public List<UserRep> findAllUser() {
		List<User> users = em.createNamedQuery("User.findAll", User.class)
				.getResultList();
		ArrayList<UserRep> retList = new ArrayList<>();
		for(User u: users) {
			retList.add(new UserRep(u));
		}
		return retList;
	}

	/**
	 * Create a new user in the database
	 * @param password The password of the user
	 * @param email The email of the user
	 * @param firstName The first name of the user
	 * @param lastName The last name of the user
	 * @param language The language of the user
	 * @param isCertified The certification of the user
	 * @param isRoot The administration rights of the user
	 * @param isBanned The banishment of the user
	 * @param registrationDate The registration date of the user
	 * @param randomIdentifier The random identifier of the user
	 * @throws DAOException 
	 */
	public void createUser(HashedPassword password, String email, String firstName, String lastName, String language,
			boolean isCertified, boolean isRoot, boolean isBanned, Date registrationDate, int randomIdentifier) throws DAOException{

		try {
			em.createNamedQuery("User.findByEmail", User.class)
			.setParameter("email", email)
			.getSingleResult();
			throw new DAOException("15");
		}catch(NoResultException e){
			
		}catch(NonUniqueResultException e){
			throw new DAOException("15");
		}catch(IllegalStateException | PersistenceException e){
			throw new DAOException("50");
		}

		try {
			utx.begin();
			User user = new User();
			user.setPasswordHash(password.getHash());
			user.setPasswordSalt(password.getSalt());
			user.setEmail(email);
			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setLanguage(language);
			user.setIsCertified(isCertified);
			user.setIsRoot(isRoot);
			user.setIsBanned(isBanned);
			user.setRegistrationDate(registrationDate);
			user.setIsRegistered(false);
			user.setRandomIdentifier(randomIdentifier);

			create(user);

			utx.commit();

		} catch (Exception e) {
			try {
				utx.rollback();
				throw new DAOException("50");
			} catch (IllegalStateException | SecurityException | SystemException e1) {
				throw new DAOException("51");
			}
		}
	}

	/**
	 * Get the password hash of a user
	 * @param email The email of the user
	 * @return The password hash
	 * @throws DAOException
	 */
	public HashedPassword getUserHash(String email) throws DAOException{
		User user;
		try {
			user = em.createNamedQuery("User.findByEmail", User.class)
					.setParameter("email", email)
					.getSingleResult();
		} catch(NoResultException e) {
			throw new DAOException("8");
		}catch(NonUniqueResultException e){
			throw new DAOException("13");
		}catch(IllegalStateException  | PersistenceException e){
			throw new DAOException("50");
		}

		return new HashedPassword(user.getPasswordHash(), user.getPasswordSalt());
	}
	
	/**
	 * Get an user by its email
	 * @param email The email of the user
	 * @return The selected user
	 * @throws DAOException 
	 */
	private User getUserByEmailPrivate(String email) throws DAOException{
		User user;
		try {
			user = em.createNamedQuery("User.findByEmail", User.class)
					.setParameter("email", email)
					.getSingleResult();

		} catch(NoResultException e) {
			throw new DAOException("8");
		} catch (Exception e) {
			throw new DAOException("50");
		}

		return user;
	}
	
	/**
	 * Get a user representation by its email
	 * @param email The email of the user
	 * @return The selected user representation
	 * @throws DAOException 
	 */
	public UserRep getUserByEmail(String email) throws DAOException{
		return new UserRep(getUserByEmailPrivate(email));
	}

	/**
	 * Change the rights of a user
	 * @param email The email of the selected user
	 * @param value The value to apply to the selected field
	 * @param action The right field to change
	 * @throws DAOException 
	 */
	public void setUserRightByEmail(String email, boolean value, String action) throws DAOException {
		try {
			User user = getUserByEmailPrivate(email);

			utx.begin();

			switch (action) {

			case "cert":
				user.setIsCertified(value);
				break;

			case "ban":
				user.setIsBanned(value);
				if (value) {
					ecv.deleteAllByUserId(user.getId());
					ekv.deleteAllByUserId(user.getId());
					elv.deleteAllByUserId(user.getId());
				}
				break;
			case "admin":
				user.setIsRoot(value);
				break;
			default:
				utx.rollback();
				return;
			}

			update(user);
			utx.commit();

		} catch (Exception e) {
			try {
				utx.rollback();
				throw new DAOException("50");
			} catch (IllegalStateException | SecurityException | SystemException e1) {
				throw new DAOException("51");
			}
		}	
	}
	
	/**
	 * Get the information of a user
	 * @param userId The id of the selected user
	 * @return The informations of the selected user
	 * @throws DAOException
	 */
	public UserObject getUserInfo(int userId) throws DAOException {
		List<WorksheetObject> worksheetObjects;
		try {
			worksheetObjects = worksheetDao.getUserWorksheets(userId);
		} catch (DAOException e) {
			if(e.getErrorCode().equals("5")){
				worksheetObjects = new ArrayList<>();
			}else{
				throw new DAOException(e.getErrorCode());
			}
		}
		User user = getUserById(userId);
		UserObject userObject = new UserObject(user, worksheetObjects);
		return userObject;
	}

	/**
	 * Validate the registration of a user
	 * @param email The email of the user
	 * @param randomIdentifier The identifier of the account
	 * @return A boolean if the validation is successful
	 * @throws DAOException 
	 */
	public boolean setIsRegister(String email, int randomIdentifier) throws DAOException {
		try {
			User user = getUserByEmailPrivate(email);
			
			if (user == null) {
				return false;
			}
			
			if (randomIdentifier == user.getRandomIdentifier()) {
				utx.begin();
				user.setIsRegistered(true);
				List<AuthorizedDomainName> domains = em.createNamedQuery("AuthorizedDomainName.findAll", AuthorizedDomainName.class)
									 				 .getResultList();
				for(AuthorizedDomainName domain: domains) {
					if(email.contains(domain.getDomain())) {
						user.setIsCertified(true);
						break;
					}
				}
				update(user);
				utx.commit();
				return true;
			}

		} catch(NoResultException | NotSupportedException | SystemException | SecurityException | IllegalStateException | RollbackException | HeuristicMixedException | HeuristicRollbackException e) {
			throw new DAOException("50");
		}
		return false;
	}
}
