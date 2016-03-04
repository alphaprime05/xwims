package service;

import java.io.UnsupportedEncodingException;
import java.security.spec.InvalidKeySpecException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonProcessingException;

import authentification.AuthChecker;
import dao.UserDao;
import dao.WorksheetDao;
import dao.WorksheetExerciseDao;
import exception.DAOException;
import exception.DAOExceptionMessage;
import objects.WorksheetObject;

@Path("")
public class Worksheet {

	@Inject
	private UserDao userDao;

	@Inject
	private WorksheetDao worksheetDao;

	@Inject
	private WorksheetExerciseDao worksheetExerciseDao;

	/**
	 * This method is used to get the worksheet information
	 * @param authorization64 the header concerning the user authentication, it permit to get the user ID
	 * @return the Worksheet information
	 * @throws UnsupportedEncodingException
	 */
	@GET
	@Path("/worksheet")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWorksheet(@HeaderParam("Authorization") String authorization64) {
		int userId;
		try {
			// Getting the ID of the user by the Header information
			userId = AuthChecker.getUserId(authorization64, userDao);
		} catch (JsonProcessingException | InvalidKeySpecException e) {
			return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", "Problem with the authentification")).build();
		}
		if(userId==-1){ /* Non connected user */
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", "Nous n'avons pas de feuille d'exercices à charger pour vous")).build();
		}else{ /* Connected user */
			// We find the last worksheet of the concerning user in the database
			WorksheetObject worksheetObject;
			try {
				worksheetObject = worksheetDao.getUserLastWorksheet(userId);
			} catch (DAOException e) {
				return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("Info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
			}
			return Response.status(Status.OK).entity(worksheetObject).build();
		}
	}

	/**
	 * This method saves the worksheet on the database
	 * @param worksheet The worksheet to save
	 * @return A response containing the info about the request.
	 */
	@POST
	@Path("/worksheet/save")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveWorksheet(@HeaderParam("Authorization") String authorization64, WorksheetObject worksheet) {
		if(worksheet==null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", "Rien à enregistrer")).build();
		}
		int ownerId;
		try {
			ownerId = AuthChecker.getUserId(authorization64, userDao);
		} catch (JsonProcessingException | InvalidKeySpecException e) {
			return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", "Probleme avec l'authentification")).build();
		}
		int worksheetId;
		try {
			worksheetId = worksheetDao.saveWorksheetInDatabase(worksheet, ownerId);
		} catch (DAOException e) {
			return Response.status(Status.EXPECTATION_FAILED).entity(getInfoMessageInJson("Info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
		}
		try {
			worksheetExerciseDao.saveWorksheetExerciseInDatabase(worksheet.getExercices_param(), worksheetId);
		} catch (DAOException e) {
			return Response.status(Status.EXPECTATION_FAILED).entity(getInfoMessageInJson("Info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
		}
		return Response.status(Status.OK).entity("{\"Titles\":\""+worksheet.getTitle()+"\"}").build();
	}

	/**
	 * Delete the specified worksheet from the database
	 * @param id The id of the specified database
	 * @return A response containing the information about the request
	 */
	@DELETE
	@Path("/worksheet/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExerciseList(@HeaderParam("Authorization") String authorization64, @PathParam("id") int id) {
		int ownerId;
		try {
			ownerId = AuthChecker.getUserId(authorization64, userDao);
		} catch (JsonProcessingException | InvalidKeySpecException e) {
			return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", "Probleme avec l'authentification")).build();
		}
		try {
			worksheetDao.deleteByIdAndUserId(ownerId, id);
		} catch (DAOException e) {
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("Info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
		}
		return Response.status(Status.OK).build();
	}

	/**
	 * This method get the generated wims code from the Worksheet
	 * @return A response containing the code of the worksheet
	 */
	@POST
	@Path("/worksheet/code")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response getWorksheetCode(WorksheetObject worksheet) {
		if(worksheet==null){
			return Response.status(Status.NO_CONTENT).entity(getInfoMessageInJson("info", "Rien à générer")).build();
		}
		if(!worksheet.getExercices_param().isEmpty()){
			String code;
			try {
				code = worksheetDao.getCode(worksheet.getExercices_param());
			} catch (DAOException e) {
				return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("Info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
			}
			return Response.status(Status.OK).entity(code).build();
		}

		return Response.status(Status.BAD_REQUEST).build();
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
