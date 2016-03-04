package service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Random;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonProcessingException;

import authentification.AuthChecker;
import authentification.UserStatus;
import dao.ConfigurationDao;
import dao.UserDao;
import exception.DAOException;
import exception.DAOExceptionMessage;
import exception.ServiceExceptionMessage;
import objects.RegisterObject;

@Path("")
public class UserManagement {
	@Inject
	private UserDao userDao;
	
	@Inject
	private ConfigurationDao confDao;

	@Resource(name = "java:jboss/mail/xwims")
	private javax.mail.Session session;


	/**
	 * Insert a user in the database
	 * @param email The email of the user
	 * @param password The password of the user
	 * @param firstName The firstname of the user
	 * @param lastName The lastname of the user
	 * @param language The language of the user
	 * @return Return a response about the status of the request
	 * @throws JsonProcessingException
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/register")
	public Response registerUser(RegisterObject registerObject) throws JsonProcessingException {
		System.out.println(registerObject);
		if(registerObject.getLang() == null || registerObject.getEmail() == null || registerObject.getPassword() == null || registerObject.getFirst_name() == null || registerObject.getLast_name() == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}

		security.HashedPassword hashedPassword;
		try {
			hashedPassword = new security.HashedPassword(registerObject.getPassword());
			//to identify an user that want to be registered but his not still registered. (he is identified by his email + his RandomIdentifier)
			Random rand = new Random();
			int randomIdentifier = rand.nextInt(Integer.MAX_VALUE);
			try {
				userDao.createUser(hashedPassword, registerObject.getEmail(), registerObject.getFirst_name(), registerObject.getLast_name(), registerObject.getLang(), false, false, false, new Date(), randomIdentifier);
			} catch (DAOException e) {
				return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getErrorCode()))).build();
			}

			String url;
			try {
				url = confDao.getServerUrl();
			} catch (DAOException e) {
				return Response.status(Status.EXPECTATION_FAILED).entity(getInfoMessageInJson("Info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
			}
			String confimationUrl = url + "xwims-webservice/confirmregister?email="+registerObject.getEmail()+"&rdm="+randomIdentifier;

			try {
				sendRegistrationEmail(registerObject.getEmail(), "Congratulation you are now a xWimer !", "\n Thank you "+registerObject.getFirst_name()+" "+registerObject.getLast_name()+
						" for your registration. Could you confirm your registration to follow this link : "+confimationUrl);
			} catch (MessagingException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("162"))).build();
			}
		} catch (NoSuchAlgorithmException|InvalidKeySpecException  e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("161"))).build();
		}

		return Response.status(200).entity(getInfoMessageInJson("register", "la demande d'inscription à bien été prise en compte")).build();
	}

	/**
	 * Send a registration mail to the user
	 * @param addresse The address of the user
	 * @param topic The topic of the message
	 * @param textMessage The content of the message
	 * @throws MessagingException 
	 * @throws AddressException 
	 */
	public void sendRegistrationEmail(String addresse, String topic, String textMessage) throws AddressException, MessagingException {
		Message message = new MimeMessage(session);
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(addresse));
		message.setSubject(topic);
		message.setText(textMessage);
		Transport.send(message);
	}

	/**
	 * Confirm the registration of the user
	 * @param email The email of the user
	 * @param randomIdentifier The identifier of the user
	 * @return Return a response containing the status of the confirmation
	 * @throws JsonProcessingException
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/confirmregister")
	public Response registerUser(@QueryParam("email") String email, @QueryParam("rdm") int randomIdentifier) throws JsonProcessingException {
		if(email == null || randomIdentifier < 0){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}

		boolean result;
		try {
			result = userDao.setIsRegister(email, randomIdentifier);
		} catch (DAOException e) {
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getErrorCode()))).build();
		}
		if (result) {
			return Response.status(200).entity("Your registration has been saved successfully : Welcome to xWims").build();
		}

		//TODO: USE NORMALIZED ERRORS
		return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", "erreur d'enregistration, veuillez contacter votre administrateur")).build();
	}

	/**
	 * User login function
	 * @param authorization64 The user identifiers
	 * @return A response containing the status of the user. NOT_CONNECTED is returned if there is a problem during authentification.
	 * @throws JsonProcessingException
	 * @throws InvalidKeySpecException
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/login")
	public Response userLogin(@HeaderParam("Authorization") String authorization64) throws JsonProcessingException, InvalidKeySpecException {
		UserStatus auth = AuthChecker.userAuth(authorization64, userDao);
		return Response.status(200).entity(auth).build();
	}

	/**
	 * Get the id of the user
	 * @param authorization64 The user identifiers
	 * @return Return the id of the user if the login is correct. Else return -1.
	 * @throws JsonProcessingException
	 * @throws InvalidKeySpecException
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getuserid")
	public Response test(@HeaderParam("Authorization") String authorization64) throws JsonProcessingException, InvalidKeySpecException {
		int id = AuthChecker.getUserId(authorization64, userDao);
		return Response.status(200).entity(id).build();
	}

	/**
	 * Change the rights of a user
	 * @param authorization64 The user identifiers
	 * @param emailToApply The email of the user rights will be changed
	 * @param value The value to apply to the selected right
	 * @param action The right to change
	 * @return A response containing the status of the request.
	 * @throws JsonProcessingException
	 * @throws InvalidKeySpecException
	 */
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/setuserright")
	public Response userLogin(@HeaderParam("Authorization") String authorization64, @QueryParam("email_to_apply") String emailToApply,
			@QueryParam("value") Boolean value, @QueryParam("action") String action) throws JsonProcessingException, InvalidKeySpecException {
		if(value == null || action == null || emailToApply == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}
		UserStatus auth = AuthChecker.userAuth(authorization64, userDao);

		if (auth.equals(UserStatus.ROOT)) {
			try {
				userDao.setUserRightByEmail(emailToApply, value, action);
			} catch (DAOException e) {
				return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getErrorCode()))).build();
			}
		} else {
			//TODO: USE NORMALIZED ERRORS
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", "L'utilisateur doit avoir les droits d'administrateur pour effecturer cette action")).build();
		}
		return Response.status(200).entity(auth).build();
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
