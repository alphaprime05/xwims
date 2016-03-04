package service;

import java.security.spec.InvalidKeySpecException;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
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
import dao.ExercisesKeywordsVoteDao;
import dao.KeywordDao;
import dao.KeywordTranslationDao;
import dao.UserDao;
import exception.DAOException;
import exception.DAOExceptionMessage;
import exception.ServiceExceptionMessage;
import objects.KeywordObject;

@Path("")
public class Keyword {

	@Inject
	private UserDao userDao;

	@Inject
	private KeywordDao keywordDao;

	@Inject
	private ExercisesKeywordsVoteDao exercisesKeywordsVoteDao;

	@Inject
	private KeywordTranslationDao keywordTranslationDao;

	/**
	 * Get the list of all the keywords beginning with the specified prefix
	 * @param language The language of the keyword looked for
	 * @param keywordBeginning The prefix looked for
	 * @return A response containing the list of all found keywords
	 */
	@GET
	//TODO: REMOVE UPERCASES IN URL
	@Path("/keywords/autocompleteUserLang")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAutoCompleteKeywordTranslation(@HeaderParam("Content-Language") String language, @QueryParam("keyword") String keywordBeginning) {
		if(language == null || keywordBeginning == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}
		List<String> keywords;

		keywords = keywordTranslationDao.getAutoComplete(keywordBeginning, language, true);

		List<String> result = new LinkedList<>(keywords);

		if(result.isEmpty()){
			return Response.status(Status.NO_CONTENT).build();
		}

		return Response.status(Status.OK).entity(result).build();
	}

	/**
	 * Get the list of all the keywords beginning with the specified prefix
	 * @param keywordBeginning The prefix looked for
	 * @return A response containing the list of all found keywords
	 */
	@GET
	@Path("/keywords/autocomplete")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAutoCompleteKeyword(@QueryParam("keyword") String keywordBeginning) {
		if(keywordBeginning == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}

		List<String> keywords;

		keywords = keywordDao.getAutoCompleteKeyword(keywordBeginning, true);

		List<String> result = new LinkedList<>(keywords);

		if(result.isEmpty()){
			return Response.status(Status.NO_CONTENT).build();
		}

		return Response.status(Status.OK).entity(result).build();
	}

	/**
	 * Create a new keyword
	 * @param language The language of the keyword
	 * @param authorization64 The authentification information of the user
	 * @param keywordObject The object containing all the information of the new keyword
	 * @return A response containing the status of the request
	 */
	@POST
	@Path("/keywords/create")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createNewKeyword(@HeaderParam("Content-Language") String language, @HeaderParam("Authorization") String authorization64, KeywordObject keywordObject) {
		if(language == null || keywordObject == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}
		int userId;
		try {
			// Getting the ID of the user by the Header information
			userId = AuthChecker.getUserId(authorization64, userDao);
		} catch (JsonProcessingException | InvalidKeySpecException e) {
			return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("121"))).build();
		}

		// BANNED, NOT_CONNECTED, CERTIFIED, ROOT, NORMAL_USER
		try {
			if(AuthChecker.userAuth(authorization64, userDao) == UserStatus.CERTIFIED || AuthChecker.userAuth(authorization64, userDao) == UserStatus.ROOT){
				if(!keywordObject.getKeyword().isEmpty() && !keywordObject.getKeyword_en().isEmpty()){
					entity.Keyword keyword = keywordDao.createKeyword(keywordObject);
					if(keyword != null){
						keywordTranslationDao.createKeywordTranslation(keywordObject.getKeyword(), language, keyword);
						exercisesKeywordsVoteDao.addKeywordVote(userId, language, keywordObject);
						return Response.status(Status.OK).build();
					}
				}
				return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("102"))).build();
			}
		} catch (JsonProcessingException | InvalidKeySpecException e) {
			return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("121"))).build();
		} catch (DAOException e) {
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getErrorCode()))).build();
		}
		return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("111"))).build();
	}

	/**
	 * Add a vote for the specified user
	 * @param language The language of the the user
	 * @param authorization64 The identifiers of the user
	 * @param keywordObject The keyword of the user
	 * @return A response containing the status of the request
	 */
	@POST
	@Path("/keywords/addVote")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addVoteExistingKeyword(@HeaderParam("Content-Language") String language, @HeaderParam("Authorization") String authorization64, KeywordObject keywordObject){
		if(language == null || keywordObject == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}

		int userId;
		try {
			// Getting the ID of the user by the Header information
			userId = AuthChecker.getUserId(authorization64, userDao);
		} catch (JsonProcessingException | InvalidKeySpecException e) {
			return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("111"))).build();
		}
		try {
			exercisesKeywordsVoteDao.addKeywordVote(userId, language, keywordObject);
			return Response.status(Status.OK).build();
		} catch (DAOException e) {
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getErrorCode()))).build();
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
