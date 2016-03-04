package dao;

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

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

import entity.Category;
import entity.CategoryTranslation;
import exception.DAOException;
import objects.CategoryObject;
import rep.CategoryRep;


@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class CategoryDao extends AbstractGenericDao<Category, String> {

	@Resource
	private UserTransaction utx;
	
	@Inject
	private CategoryTranslationDao categoryTranslationDao;
	
	public CategoryDao() {
		super(Category.class);
	}

	/**
	 * Get the autocomplete of categories begin by start send in parameter.
	 * @param start the begining of the word searched
	 * @param enableUnusedCategories if true take all categories otherwise take only the categories associate to exercise
	 * @return a list of categories name begin by the letters send in parameter
	 */
	public List<String> getAutoCompleteAttachableCategory(String start, boolean enableUnusedCategories) {
		start = start.toLowerCase();
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
		QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Category.class).get();
		org.apache.lucene.search.Query query = qb.keyword().wildcard().onField("wimsEnName").matching(start + "*").createQuery();

		javax.persistence.Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, Category.class);

		@SuppressWarnings("unchecked")
		List<Category> queryResult = persistenceQuery.getResultList();

		LinkedList<String> methodResult = new LinkedList<>();
		for(Category category : queryResult){
			if((!enableUnusedCategories && category.getExercisesCategoryVotes().size() == 0)) {
				continue;
			}
			if(category.getExercisesCanBeAttached()){
				methodResult.add(category.getWimsEnName());
			}
		}
		return methodResult;
	}
	
	/**
	 * Create a new category in database if the parent_category equals to -1 we create a top category otherwise we create a category with a parent
	 * @param categoryObject have the information : the category to add, the exercise concerned, the parent category id
	 * @return a replication of the category created
	 * @throws DAOException if we have problem with the creation or if the parent_category not exist in database
	 */
	public CategoryRep createCategoryAttachable(CategoryObject categoryObject) throws DAOException {
		if(categoryObject.getId_parent_category() == -1){
			try{
				utx.begin();
				Category category = new Category();
				category.setExercisesCanBeAttached(false);
				category.setWimsEnName(categoryObject.getCategory_en());
				category.setParentCategory(null);
				create(category);
				utx.commit();
				return new CategoryRep(category);
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
		CategoryTranslation categoryTranslation = categoryTranslationDao.getCategoryTranslationById(categoryObject.getId_parent_category());
		Category parentCategory = categoryTranslation.getCategory();
		try{
			utx.begin();
			Category category = new Category();
			category.setExercisesCanBeAttached(true);
			category.setWimsEnName(categoryObject.getCategory_en());
			category.setParentCategory(parentCategory);
			create(category);
			utx.commit();
			return new CategoryRep(category);
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
	 * Get the category by is name
	 * @param name of the category
	 * @return the Category
	 * @throws DAOException if we have no result for the name
	 */
	public Category getCategoryByName(String name) throws DAOException{
		try{
			return em.createNamedQuery("Category.findCategoryByName", Category.class)
					.setParameter("name", name)
					.getSingleResult();
		}catch(NoResultException e){
			throw new DAOException("60");
		}catch(NonUniqueResultException e){
			return em.createNamedQuery("Category.findCategoryByName", Category.class)
					.setParameter("name", name)
					.getResultList().get(0);
		}catch(IllegalStateException | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}

	/**
	 * Get the category By name and language
	 * @param language of the user
	 * @param name of the category
	 * @return the category
	 * @throws DAOException if we have no result for the language and name send in parameter
	 */
	public Category getCategoryByNameAndLanguage(String language, String name) throws DAOException{
		try{
			return em.createNamedQuery("CategoryTranslation.findByName", CategoryTranslation.class)
					.setParameter("language", language)
					.setParameter("name", name)
					.getSingleResult()
					.getCategory();
		}catch(NoResultException e){
			throw new DAOException("60");
		}catch(NonUniqueResultException e){
			return em.createNamedQuery("CategoryTranslation.findByName", CategoryTranslation.class)
					.setParameter("language", language)
					.setParameter("name", name)
					.getResultList().get(0)
					.getCategory();
		}catch(IllegalStateException | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}
}
