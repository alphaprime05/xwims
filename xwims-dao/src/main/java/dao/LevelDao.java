package dao;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.query.dsl.QueryBuilder;

import entity.ExercisesLevelsVote;
import entity.Level;
import exception.DAOException;
import rep.ExerciseRep;
import rep.LevelRep;

@Stateless
public class LevelDao extends AbstractGenericDao<Level, String> {

	@Inject
	private ExerciseDao exerciseDao;

	public LevelDao() {
		super(Level.class);
	}

	/**
	 * Get a list of all level
	 * @return a list of levels
	 */
	public List<LevelRep> findAllLevels() {
		List<Level> levels = em.createNamedQuery("Level.findAll", Level.class).getResultList();
		ArrayList<LevelRep> retList = new ArrayList<>();
		for(Level l: levels) {
			retList.add(new LevelRep(l));
		}
		return retList; 
	}

	/**
	 * Create a level
	 * @param wimsName The level name
	 */
	public void createLevel(String wimsName){
		if(wimsName != null){
			Level c = new Level();
			c.setWimsName(wimsName);
			create(c);
		}
	}

	/**
	 * Get the autocomplete of level begin by start send in parameter.
	 * @param start the begining of the word searched
	 * @param enableUnusedKeywords if true take all level otherwise take only the levels associate to the exercise
	 * @return a list of level name begin by the letters send in parameter
	 */
	public List<String> getAutoComplete(String start, boolean enableUnusedKeywords) {
		start = start.toUpperCase();
		FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
		QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Level.class).get();

		Query query = qb.keyword().wildcard().onField("wimsName").matching(start + "*").createQuery();

		javax.persistence.Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, Level.class);

		@SuppressWarnings("unchecked")
		List<Level> queryResult = persistenceQuery.getResultList();

		LinkedList<String> methodResult = new LinkedList<>();
		for(Level l : queryResult){
			if((!enableUnusedKeywords && l.getExercisesLevelsVotes().size() == 0)) {
				continue;
			}
			methodResult.add(l.getWimsName());
		}		
		return methodResult;
	}

	/**
	 * Search for the list of exercises linked to a translation
	 * @param word The translation of the level
	 * @param language The language of translation
	 * @return The list of exercises
	 * @throws DAOException
	 */
	public List<ExerciseRep> getListOfExercisesLinkedToTranslation(String word, String language) throws DAOException {
		word = word.toUpperCase();
		LinkedList<ExerciseRep> returnList = new LinkedList<>();

		FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
		QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Level.class).get();

		Query query = qb.phrase().onField("wimsName").sentence(word).createQuery();
		javax.persistence.Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, Level.class);
		@SuppressWarnings("unchecked")
		List<Level> queryResult = persistenceQuery.getResultList();

		if(queryResult.size() == 0) {
			return returnList;
		}

		for(ExercisesLevelsVote e: queryResult.get(0).getExercisesLevelsVotes()) {
			returnList.add(exerciseDao.getExerciseById(e.getExercise().getId(), language));
		}

		return returnList;	
	}
}
