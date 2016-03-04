package dao;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.query.dsl.QueryBuilder;

import entity.Keyword;
import entity.KeywordTranslation;
import exception.DAOException;
import objects.KeywordObject;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class KeywordDao extends AbstractGenericDao<Keyword, String>{

	@Resource
	private UserTransaction utx;
	
	public KeywordDao() {
		super(Keyword.class);
	}
	
	/**
	 * Get keyword by name
	 * @param name The keyword name
	 * @return the keyword
	 * @throws DAOException if no result for the id send in parameter or a problem with the request to the database
	 */
	public Keyword getKeywordByName(String name) throws DAOException{
		try{
			return em.createNamedQuery("Keyword.findByName", Keyword.class)
					.setParameter("wimsEnName", name)
					.getSingleResult();
		}catch(NoResultException e){
			throw new DAOException("30");
		}catch(NonUniqueResultException e){
			return em.createNamedQuery("Keyword.findByName", Keyword.class)
					.setParameter("wimsEnName", name)
					.getResultList().get(0);
		}catch(IllegalStateException | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}
	
	/**
	 * Get Keyword by name and language
	 * @param language The language of the keyword
	 * @param name The keyword name
	 * @return the keyword
	 * @throws DAOException if no result for the id send in parameter or a problem with the request to the database
	 */
	public Keyword getKeywordByNameAndLanguage(String language, String name) throws DAOException{
		try{
			return em.createNamedQuery("KeywordTranslation.findKeywordByTranslation", KeywordTranslation.class)
					 .setParameter("translation", name)
					 .setParameter("language", language)
					 .getSingleResult()
					 .getKeyword();
		}catch(NoResultException e){
			throw new DAOException("30");
		}catch(NonUniqueResultException e){
			return em.createNamedQuery("KeywordTranslation.findKeywordByTranslation", KeywordTranslation.class)
					 .setParameter("translation", name)
					 .setParameter("language", language)
					 .getResultList().get(0)
					 .getKeyword();
		}catch(IllegalStateException | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}

	/**
	 * Get the autocomplete of keywords begin by start send in parameter.
	 * @param start the begining of the word searched
	 * @param enableUnusedKeywords if true take all keywords otherwise take only the keywords associate to the exercise
	 * @return a list of keywords name begin by the letters send in parameter
	 */
	public List<String> getAutoCompleteKeyword(String start, boolean enableUnusedKeywords) {
		start = start.toLowerCase();
		FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
		QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Keyword.class).get();
		
		Query query = qb.keyword().wildcard().onField("wimsEnName").matching(start + "*").createQuery();
		
		javax.persistence.Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, Keyword.class);

		@SuppressWarnings("unchecked")
		List<Keyword> queryResult = persistenceQuery.getResultList();
		
		LinkedList<String> methodResult = new LinkedList<>();
		for(Keyword keyword : queryResult){
			if((!enableUnusedKeywords && keyword.getExercisesKeywordsVotes().size() == 0)) {
				continue;
			}
			methodResult.add(keyword.getWimsEnName());
		}
		return methodResult;
	}

	/**
	 * Create a new keyword
	 * @param keywordObject have the information like the keyword name to add, the exercise concerned
	 * @return the keyword added
	 * @throws DAOException if a problem occurred during the creation
	 */
	public entity.Keyword createKeyword(KeywordObject keywordObject) throws DAOException {
		try{
			utx.begin();
			Keyword keyword = new Keyword();
			keyword.setWimsEnName(keywordObject.getKeyword_en());
			create(keyword);
			utx.commit();
			return keyword;
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
