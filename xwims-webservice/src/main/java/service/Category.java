package service;

import java.security.spec.InvalidKeySpecException;
import java.util.LinkedList;
import java.util.List;

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
import dao.CategoryDao;
import dao.CategoryTranslationDao;
import dao.ExercisesCategoryVoteDao;
import dao.NextCategoryForExerciseDao;
import dao.PreviousCategoryForExerciseDao;
import dao.UserDao;
import exception.DAOException;
import exception.DAOExceptionMessage;
import exception.ServiceExceptionMessage;
import objects.CategoryObject;
import rep.CategoryRep;
import rep.CategoryTranslationRep;

@Path("")
public class Category {

	@Inject
	private CategoryDao categoryDao;

	@Inject
	private CategoryTranslationDao categoryTranslationDao;

	@Inject
	private UserDao userDao;

	@Inject
	private ExercisesCategoryVoteDao exercisesCategoryVoteDao;
	
	@Inject
	private NextCategoryForExerciseDao nextCategoryForExerciseDao;

	@Inject
	private PreviousCategoryForExerciseDao previousCategoryForExerciseDao;
	
	/**
	 * Get the top categories of the tree in the user language
	 * @param language of the user
	 * @return a JSON containing the list of the top categories
	 */
	@GET
	@Path("/categories")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTopCategories(@HeaderParam("Content-Language") String language){
		if(language == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}
		try {
			List<CategoryTranslationRep> categoryTranslation = categoryTranslationDao.getTopCategory(language);
			return Response.status(Status.OK).entity(categoryTranslation).build();
		} catch (DAOException e) {
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getErrorCode()))).build();
		}
	}

	/**
	 * Get the subCategories of the CategoryTranslationId send in parameter
	 * @param language of the user
	 * @param categoryTranslationId
	 * @return the sub-categories of the category send in parameter 
	 */
	@GET
	@Path("/categories/{id}/subcategories")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSubCategories(@HeaderParam("Content-Language") String language, @PathParam("id") int categoryTranslationId){
		if(language == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}			
		try {
			List<CategoryTranslationRep> categoryTranslation = categoryTranslationDao.getSubCategories(categoryTranslationId, language);
			return Response.status(Status.OK).entity(categoryTranslation).build();
		} catch (DAOException e) {
			return Response.status(Status.NO_CONTENT).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getErrorCode()))).build();
		}
	}
	
	/**
	 * This method was called in step 1 of the add category functionality, he send a autocomplete result for the categoryBeginning in the language of the user
	 * @param language of the user
	 * @param categoryBeginning
	 * @return the categories names in user language correspond to the categoryBeginning
	 */
	@GET
	@Path("/categories/autocompleteUserLang")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAutoCompleteTranslationCategory(@HeaderParam("Content-Language") String language, @QueryParam("category") String categoryBeginning) {
		if(language == null || categoryBeginning == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}
		List<String> categoryKeywords;

		categoryKeywords = categoryTranslationDao.getAutoCompleteAttachableCategoryTranslation(categoryBeginning, language, true);

		List<String> result = new LinkedList<>(categoryKeywords);

		if(result.isEmpty()){
			return Response.status(Status.NO_CONTENT).build();
		}

		return Response.status(Status.OK).entity(result).build();
	}


	/**
	 * This method was called in step 2 of the add category functionality, he send a autocomplete result for the categoryBeginning in the default language (English)
	 * @param categoryBeginning
	 * @return the categories names correspond to the categoryBeginning
	 */
	@GET
	@Path("/categories/autocomplete")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAutoCompleteCategory(@QueryParam("category") String categoryBeginning) {
		if(categoryBeginning == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}

		List<String> categoryKeywords;
		
		categoryKeywords = categoryDao.getAutoCompleteAttachableCategory(categoryBeginning, true);

		List<String> result = new LinkedList<>(categoryKeywords);

		if(result.isEmpty()){
			return Response.status(Status.NO_CONTENT).build();
		}

		return Response.status(Status.OK).entity(result).build();
	}
	
	/**
	 * This method is called in step 3 of the add category functionality, he create a new category and then add a vote
	 * @param language of the user
	 * @param authorization64 the authentication informations
	 * @param categoryObject have the information : the category to add, the exercise concerned, the parent category id
	 * @return Forbidden if the user have no rights to create a category, Bad_Request if the parameters are null or incorrect, Ok if the creation succeed
	 */
	@POST
	@Path("/categories/create")
	public Response createNewCategory(@HeaderParam("Content-Language") String language, @HeaderParam("Authorization") String authorization64, CategoryObject categoryObject) {
		if(language == null || categoryObject == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}
		int userId;
		try {
			// Getting the ID of the user by the Header information
			userId = AuthChecker.getUserId(authorization64, userDao);
		} catch (JsonProcessingException | InvalidKeySpecException e) {
			return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("121"))).build();
		}

		try {
			if(AuthChecker.userAuth(authorization64, userDao) == UserStatus.CERTIFIED || AuthChecker.userAuth(authorization64, userDao) == UserStatus.ROOT){
				if(!categoryObject.getCategory().isEmpty() && !categoryObject.getCategory_en().isEmpty()){
					CategoryRep category = categoryDao.createCategoryAttachable(categoryObject);
					if(category != null){
						categoryTranslationDao.createCategoriesTranslation(categoryObject.getCategory(), language, category);
						if(categoryObject.getId_parent_category() != -1){
							exercisesCategoryVoteDao.addCategoryVote(userId, language, categoryObject);
						}
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
	 * This method is used in step 1 and 2 of the add category functionality and also when a user want to vote for this category and exercise. 
	 * It permit to add a vote for the category for the specific exercise send in parameter by the user
	 * @param language of the user
	 * @param authorization64 the authentication informations
	 * @param categoryObject have the information : the category to add, the exercise concerned, the parent category id
	 * @return Forbidden if the user have no rights to add vote, Bad_Request if the parameters are null or incorrect, Ok if the vote succeed
	 */
	@POST
	@Path("/categories/addVote")
	public Response addVoteExistingCategory(@HeaderParam("Content-Language") String language, @HeaderParam("Authorization") String authorization64, CategoryObject categoryObject){
		if(language == null || categoryObject == null){
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
			exercisesCategoryVoteDao.addCategoryVote(userId, language, categoryObject);
			return Response.status(Status.OK).build();
		} catch (DAOException e) {
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getErrorCode()))).build();
		}
	}
	
	/**
	 * This method permit to add a next category to do for a specific exercise
	 * @param language of the user
	 * @param authorization64 the authentication informations
	 * @param categoryTranslationId the category proposed
	 * @param exerciseId the exercise concerned
	 * @return Forbidden if the user have no rights to propose a next category, Bad_Request if the parameters are null or incorrect, Ok if the proposition succeed
	 */
	@POST
	@Path("/categories/{id}/addNextCategoryForExercise")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addNextCategoryForExercise(@HeaderParam("Content-Language") String language, @HeaderParam("Authorization") String authorization64, @PathParam("id") int categoryTranslationId, @QueryParam("exerciseId") Integer exerciseId){
		if(language == null || exerciseId == null){
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
			try {
				if(AuthChecker.userAuth(authorization64, userDao) == UserStatus.CERTIFIED || AuthChecker.userAuth(authorization64, userDao) == UserStatus.ROOT){
					/*This test permit to verify if the user have already propose this category for this exercise*/
					if(nextCategoryForExerciseDao.isUserCanAddNextCategoryForExercise(userId, language, categoryTranslationId, exerciseId)){
						nextCategoryForExerciseDao.addNextCategoryForExercise(userId, language, categoryTranslationId, exerciseId);
						return Response.status(Status.OK).build();
					}else{
						return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("151"))).build();
					}
				}
				return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("111"))).build();
			} catch (JsonProcessingException | InvalidKeySpecException e) {
				return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("111"))).build();
			}
		} catch (DAOException e) {
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getErrorCode()))).build();
		}
	}
	
	/**
	 * Add a previous category to do for a specific exercise
	 * @param language of the user
	 * @param authorization64 the authentication informations
	 * @param categoryTranslationId the category proposed
	 * @param exerciseId the exercise concerned
	 * @return Forbidden if the user have no rights to propose a previous category, Bad_Request if the parameters are null or incorrect, Ok if the proposition succeed
	 */
	@POST
	@Path("/categories/{id}/addPreviousCategoryForExercise")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addPreviousCategoryExercise(@HeaderParam("Content-Language") String language, @HeaderParam("Authorization") String authorization64, @PathParam("id") int categoryTranslationId, @QueryParam("exerciseId") Integer exerciseId){
		if(language == null || exerciseId == null){
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
			try {
				if(AuthChecker.userAuth(authorization64, userDao) == UserStatus.CERTIFIED || AuthChecker.userAuth(authorization64, userDao) == UserStatus.ROOT){
					/*This test permit to verify if the user have already propose this category for this exercise*/
					if(previousCategoryForExerciseDao.isUserCanAddPreviousCategoryForExercise(userId, language, categoryTranslationId, exerciseId)){
						previousCategoryForExerciseDao.addPreviousCategoryForExercise(userId, language, categoryTranslationId, exerciseId);
						return Response.status(Status.OK).build();
					}else{
						return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("151"))).build();
					}
				}
				return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("111"))).build();
			} catch (JsonProcessingException | InvalidKeySpecException e) {
				return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("111"))).build();
			}
		} catch (DAOException e) {
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getErrorCode()))).build();
		}
	}

	/**
	 * Create JSON messages
	 * @param msgType like info, error, ...
	 * @param msgContent the message
	 * @return the message in Json format
	 */
	private String getInfoMessageInJson(String msgType, String msgContent){
		return "{\""+msgType+"\": \""+msgContent+"\"}";
	}
}




