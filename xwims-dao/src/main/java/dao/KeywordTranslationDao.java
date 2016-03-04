package dao;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.query.dsl.QueryBuilder;

import entity.ExercisesKeywordsVote;
import entity.Keyword;
import entity.KeywordTranslation;
import exception.DAOException;
import rep.ExerciseRep;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class KeywordTranslationDao extends AbstractGenericDao<KeywordTranslation, String> {
	
	@Resource
	private UserTransaction utx;
	
	@Inject
	private ExerciseDao exerciseDao;
	
	public KeywordTranslationDao() {
		super(KeywordTranslation.class);
	}

	/**
	 * Search all keywords translations beginning with the specified prefix.
	 * @param start The prefix to look for
	 * @param language The language of the prefix to look for
	 * @return The list of all found results
	 */
	public List<String> getAutoComplete(String start, String language, boolean enableUnusedKeywords) {
		start = start.toLowerCase();
		FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
		QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(KeywordTranslation.class).get();
		
		Query query = qb.keyword().wildcard().onField("xwimsTranslation").matching(start + "*").createQuery();
		
		javax.persistence.Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, KeywordTranslation.class);

		@SuppressWarnings("unchecked")
		List<KeywordTranslation> queryResult = persistenceQuery.getResultList();
		
		LinkedList<String> methodResult = new LinkedList<>();
		for(KeywordTranslation kt : queryResult){
			if((!enableUnusedKeywords && kt.getKeyword().getExercisesKeywordsVotes().size() == 0) || !kt.getXwimsLanguage().equals(language)) {
				continue;
			}
			methodResult.add(kt.getXwimsTranslation());
		}		
		return methodResult;
	}
	
	/**
	 * Search for the list of exercises linked to a translation
	 * @param word The translation of the keyword
	 * @param language The language of translation
	 * @return The list of exercises
	 * @throws DAOException 
	 */
	public List<ExerciseRep> getListOfExercisesLinkedToTranslation(String word, String language) throws DAOException {
		LinkedList<ExerciseRep> returnList = new LinkedList<>();
		
		FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
		QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(KeywordTranslation.class).get();
		
		Query query = qb.phrase().onField("xwimsTranslation").sentence(word).createQuery();
		javax.persistence.Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, KeywordTranslation.class);
		@SuppressWarnings("unchecked")
		List<KeywordTranslation> queryResult = persistenceQuery.getResultList();
		
		if(queryResult.size() == 0) {
			return returnList;
		}


		for(ExercisesKeywordsVote e: queryResult.get(0).getKeyword().getExercisesKeywordsVotes()) {
			returnList.add(exerciseDao.getExerciseById(e.getExercise().getId(), language));
		}
		
		return returnList;
	}

	/**
	 * Create the keyword translation in the database
	 * @param xwimsTranslation the translation of the keyword name
	 * @param language The keyword language
	 * @param keyword to save in database
	 * @throws DAOException if a problem occurred during the saving transaction
	 */
	public void createKeywordTranslation(String xwimsTranslation, String language, Keyword keyword) throws DAOException {
		try{
			utx.begin();
			KeywordTranslation keywordTranslation = new KeywordTranslation();
			keywordTranslation.setXwimsTranslation(xwimsTranslation);
			keywordTranslation.setXwimsLanguage(language);
			keywordTranslation.setKeyword(keyword);
			create(keywordTranslation);
			utx.commit();
		}catch(NotSupportedException | SecurityException | IllegalStateException | RollbackException | HeuristicMixedException
				| HeuristicRollbackException | SystemException e){
			try {
				utx.rollback();
			} catch (IllegalStateException | SecurityException | SystemException e1) {
				throw new DAOException("9");
			}
			throw new DAOException("9");
		}
	}
}
