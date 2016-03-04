package authentification;

import java.security.spec.InvalidKeySpecException;
import java.util.Base64.Decoder;

import com.fasterxml.jackson.core.JsonProcessingException;

import dao.UserDao;
import rep.UserRep;

/**
 * AuthChecker is the object handling authentification. 
 */
public class AuthChecker {
	private static Object lock = new Object();

	/**
	 * User login function
	 * @param authorization64 The user identifiers
	 * @param userDao The UserDao
	 * @return The status of the user. NOT_CONNECTED is returned if there is a problem during authentification.
	 * @throws JsonProcessingException
	 * @throws InvalidKeySpecException
	 */
	public static UserStatus userAuth(String authorization64, UserDao userDao) throws JsonProcessingException, InvalidKeySpecException {
		if (authorization64 == null || !authorization64.startsWith("Basic")) {
			return UserStatus.NOT_CONNECTED;
		}

		String basic64Credentials = authorization64.substring("Basic".length()).trim();
		boolean ret = false;
		Decoder decoder = java.util.Base64.getDecoder();
		String decodedBy = new String(decoder.decode(basic64Credentials));
		String[] parsedAuth = decodedBy.split(":");
		if(parsedAuth.length < 2) {
			return UserStatus.NOT_CONNECTED;
		}

		String email = parsedAuth[0];
		String password = parsedAuth[1];
		UserRep user = null;
		try {
			security.HashedPassword dbPassword;
			synchronized (lock) {
				dbPassword = userDao.getUserHash(email);
				user = userDao.getUserByEmail(email);
			}

			if(dbPassword.comparePasswords(password) && user.getIsRegistered()) {
				ret = true;
			}
		} catch (Exception  e) {

		}

		if(ret == false) {
			return UserStatus.NOT_CONNECTED;
		}

		if(user.getIsBanned()) {
			return UserStatus.BANNED;
		}

		if(user.getIsRoot()) {
			return UserStatus.ROOT;
		}

		if(user.getIsCertified()) {
			return UserStatus.CERTIFIED;
		}

		return UserStatus.NORMAL_USER;
	}
	/**
	 * This function authenticate the user and return its id. 
	 * @param authorization64 The user identifiers
	 * @param userDao The UserDao
	 * @return The id of the user. -1 is returned if there is a problem during authentification.
	*/
	public static int getUserId(String authorization64, UserDao userDao) throws JsonProcessingException, InvalidKeySpecException {
		if (authorization64 == null || !authorization64.startsWith("Basic")) {
			return -1;
		}

		String basic64Credentials = authorization64.substring("Basic".length()).trim();
		boolean ret = false;
		Decoder decoder = java.util.Base64.getDecoder();
		String decodedBy = new String(decoder.decode(basic64Credentials));
		String[] parsedAuth = decodedBy.split(":");
		if(parsedAuth.length < 2) {
			return -1;
		}

		String email = parsedAuth[0];
		String password = parsedAuth[1];
		UserRep user = null;
		try {
			security.HashedPassword dbPassword;
			synchronized (lock) {
				dbPassword = userDao.getUserHash(email);
				user = userDao.getUserByEmail(email);
			}

			if(dbPassword.comparePasswords(password) && user.getIsRegistered()) {
				ret = true;
			}
		} catch (Exception  e) {
		}

		if(ret == false) {
			return -1;
		}

		return user.getId();
	}
}
