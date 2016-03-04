package service;

import java.security.spec.InvalidKeySpecException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonProcessingException;

import authentification.AuthChecker;
import dao.UserDao;
import exception.DAOException;
import exception.DAOExceptionMessage;
import objects.UserObject;

@Path("")
public class UserProfile {

	@Inject
	private UserDao userDao;

	/**
	 * Get information about the connected user
	 * @param authorization64 The login information of the user
	 * @return A response containing all the information about the user.
	 */
	@GET
	@Path("/users/monProfil")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserInfo(@HeaderParam("Authorization") String authorization64) {
		int userId;
		try {
			userId = AuthChecker.getUserId(authorization64, userDao);
		} catch (JsonProcessingException | InvalidKeySpecException e) {
			return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", "Probleme avec l'authentification")).build();
		}
		try {
			UserObject userObject = userDao.getUserInfo(userId);
			return Response.status(Status.OK).entity(userObject).build();
		} catch (DAOException e) {
			return Response.status(Status.EXPECTATION_FAILED).entity(getInfoMessageInJson("Info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
		}
	}

	/**
	 * this method creates JSON messages
	 * @param msgType Defines the type of message
	 * @param msgContent Defines the content of the message
	 * @return the message in Json format
	 */
	private String getInfoMessageInJson(String msgType, String msgContent){
		return "{\""+msgType+"\": \""+msgContent+"\"}";
	}
}
