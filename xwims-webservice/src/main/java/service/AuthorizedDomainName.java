package service;

import java.security.spec.InvalidKeySpecException;

import javax.inject.Inject;
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
import dao.AuthorizedDomainNameDao;
import dao.UserDao;
import exception.DAOException;
import exception.DAOExceptionMessage;
import exception.ServiceExceptionMessage;

@Path("")
public class AuthorizedDomainName {

	@Inject
	private UserDao userDao;
	
	@Inject
	private AuthorizedDomainNameDao authorizedDomainNameDao;
	
	/**
	 * Add a new authorized domain by Root
	 * @param authorization64
	 * @param domainName The domain name
	 * @return
	 */
	@POST
	@Path("/administration/addAuthorizedDomain")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addAuthorizedDomain(@HeaderParam("Authorization") String authorization64, @QueryParam("domainName") String domainName) {
		if(domainName == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}
		try {
			if(AuthChecker.userAuth(authorization64, userDao) == UserStatus.ROOT){
				authorizedDomainNameDao.addAuthorizedDomain(domainName);
				return Response.status(Status.OK).build();
			}else{
				return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", "You have no rights to do that")).build();
			}
		} catch (DAOException e) {
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("Info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
		} catch (JsonProcessingException | InvalidKeySpecException e) {
			return Response.status(Status.FORBIDDEN).entity(getInfoMessageInJson("info", "You have no rights to do that")).build();
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
