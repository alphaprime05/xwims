package service;

import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonProcessingException;

import authentification.AuthChecker;
import authentification.UserStatus;
import dao.ExerciseDao;
import dao.ExercisesScoreVoteDao;
import dao.UserDao;
import dao.WorksheetDao;
import exception.DAOException;
import exception.DAOExceptionMessage;
import exception.ServiceExceptionMessage;
import rep.CategoryTranslationRep;
import rep.ExerciseRep;
import rep.KeywordTranslationRep;

@Path("")
public class Exercise {

	@Inject
	private ExerciseDao exerciseDao;

	@Inject
	private WorksheetDao worksheetDao;

	@Inject
	private UserDao userDao;

	@Inject
	private ExercisesScoreVoteDao exercisesScoreVoteDao;

	/**
	 * Find all the exercises in the selected language
	 * @param language The language of the user
	 * @return A Response containing all the exercises
	 * @throws JsonProcessingException
	 */
	@GET
	@Path("/exercises")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExerciseList(@HeaderParam("Content-Language") String language) throws JsonProcessingException {
		if(language == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}

		List<ExerciseRep> ex;
		try {
			ex = exerciseDao.findAllExercises(language);
		} catch (DAOException e) {
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
		}
		return Response.status(Status.OK).entity(ex).build();
	}

	/**
	 * Get the list of all equivalents exercises to the one selected
	 * @param language The language of the user
	 * @param idExercise The id of the exercise
	 * @return A response containing all the equivalent exercises.
	 */
	@GET
	@Path("/exercises/{idExercise}/equivalents")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExerciseEquivalents(@HeaderParam("Content-Language") String language, @PathParam("idExercise") int idExercise) {
		if(language == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}
		Map<ExerciseRep, Integer> map;
		try {
			map = worksheetDao.getCommonExercises(idExercise, language);
		} catch (DAOException e) {
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
		}
		List<ExerciseRep> result = new ArrayList<ExerciseRep>();
		List<Map.Entry<ExerciseRep, Integer>> equivalentsList = new ArrayList<Map.Entry<ExerciseRep, Integer>>(map.entrySet());
		Collections.sort(equivalentsList, new Comparator<Map.Entry<ExerciseRep, Integer>>()
		{
			public int compare(Map.Entry<ExerciseRep, Integer> o1, Map.Entry<ExerciseRep, Integer> o2)
			{
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		int i = 0;
		for (Map.Entry<ExerciseRep, Integer> entry : equivalentsList)
		{
			if( i>=5) {
				break;
			}
			result.add(entry.getKey());
			i++;
		}
		return Response.status(Status.OK).entity(result).build();
	}

	/**
	 * Get the information of the specified exercise
	 * @param language The language of the user
	 * @param idExercise The id of the exercise
	 * @return A response containing the specified exercise
	 */
	@GET
	@Path("/exercises/{idExercise}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExerciseById(@HeaderParam("Content-Language") String language, @PathParam("idExercise") int idExercise) {
		if(language == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}
		ExerciseRep exerciseRep;
		try {
			exerciseRep = exerciseDao.getExerciseById(idExercise, language);
		} catch (DAOException e) {
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
		}
		if(exerciseRep == null){
			return Response.status(Status.NO_CONTENT).build();	
		}
		return Response.status(Status.OK).entity(exerciseRep).build();
	}

	/**
	 * Get of the categories of the specified exercise
	 * @param authorization64 The authentification information
	 * @param language The language of the user
	 * @param idExercise The id of the selected exercise
	 * @return A response containing the list of all the categories linked to the exercise.
	 */
	@GET
	@Path("/exercises/{idExercise}/categories")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCategoriesOfExerciseByExerciseId(@HeaderParam("Authorization") String authorization64, @HeaderParam("Content-Language") String language, @PathParam("idExercise") Integer idExercise) {
		if(language == null || idExercise == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}
		int userId;
		try {
			// Getting the ID of the user by the Header information
			userId = AuthChecker.getUserId(authorization64, userDao);
		} catch (JsonProcessingException | InvalidKeySpecException e) {
			return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("111"))).build();
		}
		List<CategoryTranslationRep> exerciseCategories;
		try {
			exerciseCategories = exerciseDao.getCategoriesTranslationsOfExercise(userId, idExercise, language);
		} catch (DAOException e) {
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
		}
		if(exerciseCategories.isEmpty()){
			return Response.status(Status.NO_CONTENT).build();	
		}
		return Response.status(Status.OK).entity(exerciseCategories).build();
	}

	/**
	 * Get of the keywords of the specified exercise
	 * @param authorization64 The authentification information
	 * @param language The language of the user
	 * @param idExercise The id of the selected exercise
	 * @return A response containing the list of all the keywords linked to the exercise.
	 */
	@GET
	@Path("/exercises/{idExercise}/keywords")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getKeywordsOfExerciseByExerciseId(@HeaderParam("Authorization") String authorization64, @HeaderParam("Content-Language") String language, @PathParam("idExercise") Integer idExercise) {
		if(language == null || idExercise == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}
		int userId;
		try {
			// Getting the ID of the user by the Header information
			userId = AuthChecker.getUserId(authorization64, userDao);
		} catch (JsonProcessingException | InvalidKeySpecException e) {
			return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("111"))).build();
		}
		List<KeywordTranslationRep> exerciseKeywords;
		try {
			exerciseKeywords = exerciseDao.getKeywordsTranslationsOfExercise(userId, idExercise, language);
		} catch (DAOException e) {
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
		}
		if(exerciseKeywords.isEmpty()){
			return Response.status(Status.NO_CONTENT).build();	
		}
		return Response.status(Status.OK).entity(exerciseKeywords).build();
	}

	/**
	 * Get of the previous categories of the specified exercise
	 * @param authorization64 The authentification information
	 * @param language The language of the user
	 * @param idExercise The id of the selected exercise
	 * @return A response containing the list of all the previous categories linked to the exercise.
	 */
	@GET
	@Path("/exercises/{idExercise}/previousCategories")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPreviousCategoriesOfExerciseByExerciseId(@HeaderParam("Authorization") String authorization64, @HeaderParam("Content-Language") String language, @PathParam("idExercise") Integer idExercise) {
		if(language == null || idExercise == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}
		int userId;
		try {
			// Getting the ID of the user by the Header information
			userId = AuthChecker.getUserId(authorization64, userDao);
		} catch (JsonProcessingException | InvalidKeySpecException e) {
			return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("111"))).build();
		}
		List<CategoryTranslationRep> exercisePreviousCategories;
		try {
			exercisePreviousCategories = exerciseDao.getPreviousCategoriesTranslationsForExerciseOfExercise(userId, idExercise, language);
		} catch (DAOException e) {
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
		}
		if(exercisePreviousCategories.isEmpty()){
			return Response.status(Status.NO_CONTENT).build();	
		}
		return Response.status(Status.OK).entity(exercisePreviousCategories).build();
	}

	/**
	 * Get of the next categories of the specified exercise
	 * @param authorization64 The authentification information
	 * @param language The language of the user
	 * @param idExercise The id of the selected exercise
	 * @return A response containing the list of all the next categories linked to the exercise.
	 */
	@GET
	@Path("/exercises/{idExercise}/nextCategories")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNextCategoriesOfExerciseByExerciseId(@HeaderParam("Authorization") String authorization64, @HeaderParam("Content-Language") String language, @PathParam("idExercise") Integer idExercise) {
		if(language == null || idExercise == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}
		int userId;
		try {
			// Getting the ID of the user by the Header information
			userId = AuthChecker.getUserId(authorization64, userDao);
		} catch (JsonProcessingException | InvalidKeySpecException e) {
			return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("111"))).build();
		}
		List<CategoryTranslationRep> exerciseNextCategories;
		try {
			exerciseNextCategories = exerciseDao.getNextCategoriesTranslationsForExerciseOfExercise(userId, idExercise, language);
		} catch (DAOException e) {
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
		}
		if(exerciseNextCategories.isEmpty()){
			return Response.status(Status.NO_CONTENT).build();	
		}
		return Response.status(Status.OK).entity(exerciseNextCategories).build();
	}

	/**
	 * Add a score for an exercise
	 * @param authorization64 The authentification information
	 * @param language The language of the user
	 * @param score The score specified by the user
	 * @return A response about all the status of the request
	 */
	@POST
	@Path("/exercises/{idExercise}/addScore")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addPreviousCategoryExercise(@HeaderParam("Authorization") String authorization64, @PathParam("idExercise") Integer idExercise, @QueryParam("score") Double score){
		if(idExercise == null || score == null){
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
			if(AuthChecker.userAuth(authorization64, userDao) == UserStatus.CERTIFIED || AuthChecker.userAuth(authorization64, userDao) == UserStatus.ROOT){
				exercisesScoreVoteDao.addScoreForExercise(userId, idExercise, score);
				return Response.status(Status.OK).build();
			}
			return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("111"))).build();
		} catch (JsonProcessingException | InvalidKeySpecException e) {
			return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("111"))).build();
		} catch (DAOException e) {
			return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getErrorCode()))).build();
		}
	}

	/**
	 * Get the score of an exercise
	 * @param idExercise The id of the selected exercise
	 * @return A response containing the score of the exercise
	 */
	@GET
	@Path("/exercises/{idExercise}/score")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getScoreOfExerciseByExerciseId(@PathParam("idExercise") Integer idExercise) {
		if(idExercise == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}
		try {
			double score = exercisesScoreVoteDao.getScoreForExerciseOfExercise(idExercise);
			return Response.status(Status.OK).entity(getInfoMessageInJson("score",score+"")).build();
		} catch (DAOException e) {
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
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
