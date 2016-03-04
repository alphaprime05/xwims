package service;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import dao.ConfigurationDao;
import exception.DAOException;
import exception.DAOExceptionMessage;

@Path("")
public class Configuration {

	@Inject
	private ConfigurationDao confDao;

	/**
	 * Get the URL of the server
	 * @return A response containing all the URL of the server.
	 */
	@GET
	@Path("/conf/serverurl")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getServerURL() {
		String url;
		try {
			url = confDao.getServerUrl();
		} catch (DAOException e) {
			return Response.status(Status.EXPECTATION_FAILED).entity(getInfoMessageInJson("Info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
		}
		
		return Response.status(Status.OK).entity(url).build();
	}
	
	/**
	 * Get the URL of the WIMS server
	 * @return A response containing all the URL of the WIMS server.
	 */
	@GET
	@Path("/conf/wimsurl")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWimsURL() {
		String url;
		try {
			url = confDao.getWimsUrl();
		} catch (DAOException e) {
			return Response.status(Status.EXPECTATION_FAILED).entity(getInfoMessageInJson("Info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
		}
		
		return Response.status(Status.OK).entity(url).build();
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
