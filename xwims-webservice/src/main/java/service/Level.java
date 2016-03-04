package service;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonProcessingException;

import dao.LevelDao;
import rep.LevelRep;

@Path("")
public class Level {

	@Inject
	private LevelDao levelDao;

	/**
	 * Get all the levels available in xwims
	 * @return A response containing the list of all the levels
	 * @throws JsonProcessingException
	 */
	@GET
	@Path("/levels")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllLevelList() throws JsonProcessingException {
		List<LevelRep> levels = levelDao.findAllLevels();
		return Response.status(Status.OK).entity(levels).build();
	}
}
