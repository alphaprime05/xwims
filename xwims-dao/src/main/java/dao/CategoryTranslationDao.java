package dao;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
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
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

import entity.Category;
import entity.CategoryTranslation;
import entity.ExercisesCategoryVote;
import exception.DAOException;
import rep.CategoryRep;
import rep.CategoryTranslationRep;
import rep.ExerciseRep;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class CategoryTranslationDao extends AbstractGenericDao<CategoryTranslation, String> {
	
	@Inject
	private ExerciseDao exerciseDao;
	
	@Resource
	private UserTransaction utx;
	
	public CategoryTranslationDao() {
		super(CategoryTranslation.class);
	}

	/**
	 * Search all categories translations beginning with the prefix send in parameter.
	 * @param start The prefix to look for
	 * @param language The language of the prefix to look for
	 * @param enableUnusedCategories
	 * @return The list of all found results
	 */
	public List<String> getAutoComplete(String start, String language, boolean enableUnusedCategories) {
		start = start.toLowerCase();
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
		QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(CategoryTranslation.class).get();
		Query query = qb.keyword().wildcard().onField("xwimsTranslation").matching(start + "*").createQuery();

		javax.persistence.Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, CategoryTranslation.class);

		@SuppressWarnings("unchecked")
		List<CategoryTranslation> queryResult = persistenceQuery.getResultList();

		LinkedList<String> methodResult = new LinkedList<>();
		for(CategoryTranslation ct : queryResult){
			if((!enableUnusedCategories && ct.getCategory().getExercisesCategoryVotes().size() == 0) || !ct.getXwimsLanguage().equals(language)) {
				continue;
			}
			methodResult.add(ct.getXwimsTranslation());
		}		
		return methodResult;
	}

	/**
	 * Search for the list of exercises linked to a translation
	 * @param word The translation of the category
	 * @param language The language of translation
	 * @return The list of exercises
	 * @throws DAOException
	 */
	public List<ExerciseRep> getListOfExercisesLinkedToTranslation(String word, String language) throws DAOException {
		LinkedList<ExerciseRep> returnList = new LinkedList<>();
		
		FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
		QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(CategoryTranslation.class).get();
		
		Query query = qb.phrase().onField("xwimsTranslation").sentence(word).createQuery();
		javax.persistence.Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, CategoryTranslation.class);
		@SuppressWarnings("unchecked")
		List<CategoryTranslation> queryResult = persistenceQuery.getResultList();
		
		if(queryResult.size() == 0) {
			return returnList;
		}


		for(ExercisesCategoryVote e: queryResult.get(0).getCategory().getExercisesCategoryVotes()) {
			returnList.add(exerciseDao.getExerciseById(e.getExercise().getId(), language));
		}
		
		return returnList;
	}

	/**
	 * Create the category translation in the database
	 * @param xwimsTranslation the translation of the category name
	 * @param language user language
	 * @param category to save in database
	 * @return a replication of the category saved
	 * @throws DAOException if a problem occurred during the saving transaction
	 */
	public CategoryTranslationRep createCategoriesTranslation(String xwimsTranslation, String language, CategoryRep category) throws DAOException{
		try{
			utx.begin();
			CategoryTranslation ct = new CategoryTranslation();
			ct.setXwimsTranslation(xwimsTranslation);
			ct.setXwimsLanguage(language);
			ct.setCategory(getCategoryById(category.getId()));
			create(ct);
			utx.commit();
			return new CategoryTranslationRep(ct);
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

	/**
	 * Search all attachable categories translations beginning with the prefix send in parameter
	 * @param start The prefix to look for
	 * @param language language The language of the prefix to look for
	 * @param enableUnusedCategories
	 * @return The list of all found results
	 */
	public List<String> getAutoCompleteAttachableCategoryTranslation(String start, String language, boolean enableUnusedCategories) {
		start = start.toLowerCase();
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
		QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(CategoryTranslation.class).get();
		org.apache.lucene.search.Query query = qb.keyword().wildcard().onField("xwimsTranslation").matching(start + "*").createQuery();

		javax.persistence.Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, CategoryTranslation.class);

		@SuppressWarnings("unchecked")
		List<CategoryTranslation> queryResult = persistenceQuery.getResultList();

		LinkedList<String> methodResult = new LinkedList<>();
		for(CategoryTranslation ct : queryResult){
			if((!enableUnusedCategories && ct.getCategory().getExercisesCategoryVotes().size() == 0) || !ct.getXwimsLanguage().equals(language)) {
				continue;
			}
			if(ct.getCategory().getExercisesCanBeAttached()){
				methodResult.add(ct.getXwimsTranslation());
			}
		}
		return methodResult;
	}

	/**
	 * Get the top categories of the tree in the user language
	 * @param language selected by the user
	 * @return list of the top categories
	 * @throws DAOException if a problem occurred during the categories search step
	 */
	public List<CategoryTranslationRep> getTopCategory(String language) throws DAOException{
		List<CategoryTranslationRep> topCategoryInGoodLanguage = new ArrayList<>();
		List<Category> topCategory = findTopCategories();
		if(!topCategory.isEmpty()){
			for (Category category : topCategory) {
				CategoryTranslation categoryTranslation = findCategoryTranlationByCategoryIdAndLanguage(category.getId(), language);

				if(categoryTranslation != null){
					CategoryTranslationRep categoryTranslationRep = new CategoryTranslationRep(categoryTranslation, 0, false);
					topCategoryInGoodLanguage.add(categoryTranslationRep);
				}
			}
		}
		return topCategoryInGoodLanguage;
	}

	/**
	 * Get the subCategories of the CategoryTranslationId send in parameter
	 * @param categoryTranslationId
	 * @param language selected by the user
	 * @return the sub categories of the categoryTranslationId
	 * @throws DAOException if the category not found in database
	 */
	public List<CategoryTranslationRep> getSubCategories(int categoryTranslationId, String language) throws DAOException{
		CategoryTranslation categoryTranslation = getCategoryTranslationById(categoryTranslationId); 

		Category category = categoryTranslation.getCategory();

		List<Category> subCategories = category.getSubCategories();

		if(subCategories != null && !subCategories.isEmpty()){
			List<CategoryTranslationRep> subCategoryTranslationReps = new ArrayList<>();
			for (Category cat  : subCategories) {
				CategoryTranslation subCategoryTranslation = findCategoryTranlationByCategoryIdAndLanguage(cat.getId(), language);

				if(subCategoryTranslation != null){
					CategoryTranslationRep categoryTranslationRep = new CategoryTranslationRep(subCategoryTranslation, 0, false);
					subCategoryTranslationReps.add(categoryTranslationRep);
				}
			}
			return subCategoryTranslationReps;
		}
		throw new DAOException("61");
	}

	/**
	 * Get the top categories of the tree
	 * @return top categories
	 * @throws DAOException if a problem occurred during the request to the database
	 */
	private List<Category> findTopCategories() throws DAOException {
		try{
			return em.createNamedQuery("Category.findTopCategories", Category.class)
				 	 .getResultList();
		}catch(IllegalStateException | PersistenceException e){
			throw new DAOException("50");
		}
	}

	/**
	 * Get the translation of the category by the category id send in parameter
	 * @param categoryTranslationId id of the category searched
	 * @return the translation correspond to the id
	 * @throws DAOException if no result for the id send in parameter or a problem with the request to the database
	 */
	public CategoryTranslation getCategoryTranslationById(int categoryTranslationId) throws DAOException{
		try{
			return em.createNamedQuery("CategoryTranslation.findById", CategoryTranslation.class)
					.setParameter("id", categoryTranslationId)
					.getSingleResult();
		}catch(NoResultException e){
			throw new DAOException("60");
		}catch(NonUniqueResultException e){
			return em.createNamedQuery("CategoryTranslation.findById", CategoryTranslation.class)
					.setParameter("id", categoryTranslationId)
					.getResultList().get(0);
		}catch(IllegalStateException | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}

	/**
	 * Get the translation of the category by the category id and the language send in parameter
	 * @param categoryId id of the category searched
	 * @param language the language selected by the user
	 * @return the translation correspond to the id and the language
	 * @throws DAOException if no result for the id send in parameter or a problem with the request to the database
	 */
	private CategoryTranslation findCategoryTranlationByCategoryIdAndLanguage(int categoryId, String language) throws DAOException {

		try{
			return em.createNamedQuery("CategoryTranslation.findCategoryTranlationByCategoryId", CategoryTranslation.class)
					.setParameter("language", language)
					.setParameter("categoryId", categoryId)
					.getSingleResult();
		}catch(NoResultException e){
			return null;
		}catch(NonUniqueResultException e){
			return em.createNamedQuery("CategoryTranslation.findCategoryTranlationByCategoryId", CategoryTranslation.class)
					.setParameter("language", language)
					.setParameter("categoryId", categoryId)
					.getResultList().get(0);
		}catch(IllegalStateException | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}
	
	/**
	 * Get the category by the category id send in parameter
	 * @param categoryId id of the category searched
	 * @return the category correspond to the id
	 * @throws DAOException if no result for the id send in parameter or a problem with the request to the database
	 */
	private Category getCategoryById(int categoryId) throws DAOException{
		try{
			return em.createNamedQuery("Category.findCategoryById", Category.class)
					.setParameter("id", categoryId)
					.getSingleResult();
		}catch(NoResultException e){
			throw new DAOException("60");
		}catch(NonUniqueResultException e){
			return em.createNamedQuery("Category.findCategoryById", Category.class)
					.setParameter("id", categoryId)
					.getResultList().get(0);
		}catch(IllegalStateException | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}
}
