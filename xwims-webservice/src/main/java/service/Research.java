package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import dao.CategoryTranslationDao;
import dao.ExerciseDao;
import dao.ExercisesScoreVoteDao;
import dao.KeywordTranslationDao;
import dao.LevelDao;
import exception.DAOException;
import exception.DAOExceptionMessage;
import exception.ServiceExceptionMessage;
import rep.ExerciseRep;

@Path("")
public class Research {
	@Inject
	private CategoryTranslationDao categoryTranslationDao;
	@Inject
	private KeywordTranslationDao keywordTranslationDao;
	@Inject
	private ExerciseDao exercise;
	@Inject
	private LevelDao level;
	@Inject
	private ExercisesScoreVoteDao exercisesScoreVoteDao;

	@GET
	@Path("/search/page/{pageNumber}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExerciseList(@HeaderParam("Content-Language") String language, @PathParam("pageNumber") String pageNumber) throws JsonProcessingException {
		if(language == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}

		List<ExerciseRep> ex;
		try {
			ex = exercise.findLimitedListExercise(15, Integer.parseInt(pageNumber), language);
		} catch (NumberFormatException e) {
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("103"))).build();
		} catch (DAOException e) {
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
		}
		return Response.status(Status.OK).entity(ex).build();
	}


	/**
	 * Return the total number of pages required to show all exercises in the database
	 * @return
	 * @throws JsonProcessingException
	 */
	@GET
	@Path("/search/totalPages")
	public Response getTotalNumberPages(@HeaderParam("Content-Language") String language) throws JsonProcessingException {
		if(language == null) {
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}
		int nbTotal;
		try {
			nbTotal = (int)Math.floor(exercise.findAllExercises(language).size()/15);
		} catch (DAOException e) {
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
		}
		return Response.status(Status.OK).entity(nbTotal+"").build();
	}


	/**
	 * Search for all keywords beginning with the specified prefix
	 * @param keywordBeginning The beginning of the keyword
	 * @param language The language of the keyword
	 * @return A response containing all the keywords found
	 */
	@GET
	@Path("/autocomplete")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWordsBeginningWith(@QueryParam("keyword") String keywordBeginning, @QueryParam("lang") String language) {
		if(language == null || keywordBeginning == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}
		keywordBeginning = StringUtils.stripAccents(keywordBeginning);

		List<String> categoryKeywords = categoryTranslationDao.getAutoComplete(keywordBeginning, language, false);
		List<String> keywordKeywords = keywordTranslationDao.getAutoComplete(keywordBeginning, language, false);
		List<String> levelKeywords = level.getAutoComplete(keywordBeginning, false);

		List<String> result = new LinkedList<>(categoryKeywords);
		result.addAll(keywordKeywords);
		result.addAll(levelKeywords);

		Collections.sort(result, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.length()-o2.length();
			}

		});


		return Response.status(Status.OK).entity(result).build();
	}

	/**
	 * Search and sort the exercises matching at least one parameter.
	 * @param keywords All the keywords, separated by a comma
	 * @param language The language used for the search
	 * @param sortby Defines the way the exercises must be sorted
	 * @param page Defines the page to return
	 * @param total Returns the size of the result instead of the result itself.
	 * @return A response containing all the exercises
	 */
	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public Response search(@QueryParam("keywords") String keywords, @QueryParam("lang") String language, @QueryParam("sortby") String sortby, @QueryParam("page") Integer page, @QueryParam("total") Boolean total) {
		if(language == null || keywords == null){
			return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", ServiceExceptionMessage.getErrorMessage("101"))).build();
		}

		if(sortby == null) {
			sortby = "";
		}

		keywords.toLowerCase();
		keywords = keywords.replaceAll("-", " ");
		String[] tokens = keywords.split(",");
		int categoryRelevance = 10;
		int keywordRelevance = 10;
		int levelRelevance = 10;
		int categoryRelevanceOffset = 5;
		int keywordRelevanceOffset = 5;
		int levelRelevanceOffset = 5;
		double titleRelevance = 3;
		double wordingRelevance = 2;


		HashMap<ExerciseRep, Double> relevance = new HashMap<>();
		/* For each keyword, we search it in categories, keywords, levels and finally in the exercise title and wording.
		 * Then we add points in function of where the keyword was found.
		 * If it's found in a category/keyword/level, then the point added are relative to the one with the strongest result(This last one will get all the points).
		 */
		for(String param: tokens) {
			/* First, we search in categories */
			List<ExerciseRep> categoriesVotes;
			HashMap<ExerciseRep, Integer> categoriesRelevanceMap = new HashMap<>();
			try {
				categoriesVotes = categoryTranslationDao.getListOfExercisesLinkedToTranslation(param, language);
			} catch (DAOException e) {
				return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
			}
			for(ExerciseRep ex: categoriesVotes) {
				if(categoriesRelevanceMap.containsKey(ex)) {
					categoriesRelevanceMap.put(ex, categoriesRelevanceMap.get(ex) + categoryRelevance);
				} else {
					categoriesRelevanceMap.put(ex, categoryRelevance);
				}
			}

			int max = -1;
			for(Integer value: categoriesRelevanceMap.values()) {
				if(max == -1) {
					max = value;
				} else if(value > max) {
					max = value;
				}
			}

			if(max != -1) {
				for(Entry<ExerciseRep, Integer> entry: categoriesRelevanceMap.entrySet()) {
					relevance.put(entry.getKey(), (double)(entry.getValue())/max + categoryRelevanceOffset);
				}
			}

			/* We search in keywords */
			List<ExerciseRep> keywordVotes;
			HashMap<ExerciseRep, Integer> keywordRelevanceMap = new HashMap<>();
			try {
				keywordVotes = keywordTranslationDao.getListOfExercisesLinkedToTranslation(param, language);
			} catch (DAOException e) {
				return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
			}
			for(ExerciseRep ex: keywordVotes) {
				if(keywordRelevanceMap.containsKey(ex)) {
					keywordRelevanceMap.put(ex, keywordRelevanceMap.get(ex) + keywordRelevance);
				} else {
					keywordRelevanceMap.put(ex, keywordRelevance);
				}
			}

			max = -1;
			for(Integer value: keywordRelevanceMap.values()) {
				if(max == -1) {
					max = value;
				} else if(value > max) {
					max = value;
				}
			}

			if(max != -1) {
				for(Entry<ExerciseRep, Integer> entry: keywordRelevanceMap.entrySet()) {
					relevance.put(entry.getKey(), (double)(entry.getValue())/max + keywordRelevanceOffset);
				}
			}

			/* We search in levels */
			List<ExerciseRep> levelsVotes;
			HashMap<ExerciseRep, Integer> levelsRelevanceMap = new HashMap<>();
			try {
				levelsVotes = level.getListOfExercisesLinkedToTranslation(param, language);
			} catch (DAOException e) {
				return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
			}
			for(ExerciseRep ex: levelsVotes) {
				if(levelsRelevanceMap.containsKey(ex)) {
					levelsRelevanceMap.put(ex, levelsRelevanceMap.get(ex) + levelRelevance);
				} else {
					levelsRelevanceMap.put(ex, levelRelevance);
				}
			}

			max = -1;
			for(Integer value: levelsRelevanceMap.values()) {
				if(max == -1) {
					max = value;
				} else if(value > max) {
					max = value;
				}
			}

			if(max != -1) {
				for(Entry<ExerciseRep, Integer> entry: levelsRelevanceMap.entrySet()) {
					relevance.put(entry.getKey(), (double)(entry.getValue())/max + levelRelevanceOffset);
				}
			}

			/* We check in title and in wording only if the word size is superior to 2 */
			if(param.length() >=2 ) {
				/* We search in title */
				List<ExerciseRep> exerciseList;
				try {
					exerciseList = exercise.searchInTitle(param, language);
				} catch (DAOException e) {
					return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
				}
				for(ExerciseRep e: exerciseList) {
					if(relevance.containsKey(e)) {
						relevance.put(e, relevance.get(e) + titleRelevance);
					} else {
						relevance.put(e, titleRelevance);
					}
				}

				/* We search in wording */
				try {
					exerciseList = exercise.searchInWording(param, language);
				} catch (DAOException e) {
					return Response.status(Status.BAD_REQUEST).entity(getInfoMessageInJson("info", DAOExceptionMessage.getErrorMessage(e.getMessage()))).build();
				}
				for(ExerciseRep e: exerciseList) {
					if(relevance.containsKey(e)) {
						relevance.put(e, relevance.get(e) + wordingRelevance);
					} else {
						relevance.put(e, wordingRelevance);
					}
				}
			}
		}

		if(total != null && total == true) {
			return Response.status(Status.OK).entity(relevance.size()).build();
		}

		ArrayList<ExerciseRep> result;
		List<Entry<ExerciseRep, Double>> relevanceList = null;

		/* We sort the map depending of the sortby var*/
		switch(sortby) {
		case "popularity":
			result = new ArrayList<ExerciseRep>();
			for(ExerciseRep e : relevance.keySet()) {
				addAndApplyWrongExercise(e, result);
			}
			Collections.sort(result, new Comparator<ExerciseRep>()
			{
				public int compare(ExerciseRep o1, ExerciseRep o2)
				{
					return o2.getPopularity() - o1.getPopularity();
				}
			});
			break;
		case "score":
			result = new ArrayList<ExerciseRep>();
			for(ExerciseRep e : relevance.keySet()) {
				addAndApplyWrongExercise(e, result);
			}
			Collections.sort(result, new Comparator<ExerciseRep>()
			{
				public int compare(ExerciseRep o1, ExerciseRep o2)
				{
					try {
						Double d1 = exercisesScoreVoteDao.getScoreForExerciseOfExercise(o1.getId());
						Double d2 = exercisesScoreVoteDao.getScoreForExerciseOfExercise(o2.getId());
						return d2.compareTo(d1);
					} catch (DAOException e) {
						return 0;
					}
				}
			});
			break;
		default:
			result = new ArrayList<ExerciseRep>();
			relevanceList = new ArrayList<Map.Entry<ExerciseRep, Double>>(relevance.entrySet());
			Collections.sort(relevanceList, new Comparator<Map.Entry<ExerciseRep, Double>>()
			{
				public int compare(Map.Entry<ExerciseRep, Double> o1, Map.Entry<ExerciseRep, Double> o2)
				{
					return (o2.getValue()).compareTo(o1.getValue());
				}
			});

			for (Entry<ExerciseRep, Double> entry : relevanceList)
			{
				addAndApplyWrongExercise(entry.getKey(), result);
			}
		}

		int nbMaxResult = 15;
		List<ExerciseRep> resultLimited;
		if(page != null){
			if(nbMaxResult*page>=result.size() )
				resultLimited = result.subList((page-1)*nbMaxResult, result.size());
			else
				resultLimited = result.subList((page-1)*nbMaxResult, nbMaxResult*page);
			return Response.status(Status.OK).entity(resultLimited).build();
		}

		return Response.status(Status.OK).entity(result).build();
	}

	public void addAndApplyWrongExercise(ExerciseRep exeRep, ArrayList<ExerciseRep> result) {
		boolean rep = exercise.isWrong(exeRep);
		if (rep) {
			exeRep.setBreakdown(true);
		}
		result.add(exeRep);
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


