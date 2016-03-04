package dao;

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

import entity.Category;
import entity.CategoryTranslation;
import entity.Exercise;
import entity.ExercisesCategoryVote;
import entity.User;
import exception.DAOException;
import objects.CategoryObject;
import rep.CategoryRep;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ExercisesCategoryVoteDao extends AbstractGenericDao<ExercisesCategoryVote, String>{

	@Resource
	private UserTransaction utx;
	
	@Inject
	private CategoryTranslationDao categoryTranslationDao;
	
	@Inject
	private CategoryDao categoryDao;
	
	@Inject
	private ExerciseDao exerciseDao;
	
	@Inject
	private UserDao userDao;

	public ExercisesCategoryVoteDao() {
		super(ExercisesCategoryVote.class);
	}

	/**
	 * Add vote for a category for the exercise send in parameter by the categoryObject
	 * @param userId The user Id
	 * @param language The language of the category
	 * @param categoryObject have the information : the category to add, the exercise concerned, the parent category id
	 * @throws DAOException if the information send in parameter are incorrect
	 */
	public void addCategoryVote(int userId, String language, CategoryObject categoryObject) throws DAOException {
		User user = userDao.getUserById(userId);
		Exercise exercise = exerciseDao.getExerciseById(categoryObject.getId_exercise());
		int foundStep = categoryObject.getFound_step();
		Category category;
		if(foundStep == 1){
			category = categoryDao.getCategoryByNameAndLanguage(language, categoryObject.getCategory());
		}else if(foundStep == 2){
			category = categoryDao.getCategoryByName(categoryObject.getCategory_en());
			if(isExistCategoryTranslation(language, category.getId())){
				throw new DAOException("20");
			}
			categoryTranslationDao.createCategoriesTranslation(categoryObject.getCategory(), language, new CategoryRep(category));
		}else if(foundStep == 3){
			category = categoryDao.getCategoryByName(categoryObject.getCategory_en());
		}else{
			throw new DAOException("62");
		}

		if(isUserCanVote(userId, language, category, exercise.getId())){
			try{
				utx.begin();
				ExercisesCategoryVote exercisesCategoryVote = new ExercisesCategoryVote();
				exercisesCategoryVote.setCategory(category);
				exercisesCategoryVote.setExercise(exercise);
				exercisesCategoryVote.setUser(user);

				create(exercisesCategoryVote);

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
		}else{
			throw new DAOException("80");
		}
	}

	/**
	 * Verify if the translation of the category already exist
	 * @param language The translation language
	 * @param CategoryId The category Id
	 * @return a boolean, true if the translation exist otherwise false
	 * @throws DAOException if a problem occurred during the request to the database or if we have wrong parameter
	 */
	private boolean isExistCategoryTranslation(String language,int CategoryId) throws DAOException{
		try{
			CategoryTranslation categoryTranslation = em.createNamedQuery("CategoryTranslation.findCategoryTranlationByCategoryId", CategoryTranslation.class)
					.setParameter("language", language)
					.setParameter("categoryId", CategoryId)
					.getSingleResult();
			if(categoryTranslation != null){
				return true;
			}
		}catch(NoResultException e){
			return false;
		}catch(NonUniqueResultException e){
			return true;
		}catch(IllegalStateException | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
		return false;
	}

	/**
	 * Verify if the user already vote for the category for the exercise
	 * @param userId The user Id
	 * @param exerciseId The exercise Id
	 * @param categoryId The category Id
	 * @return a boolean, true if the user already vote otherwise false
	 * @throws DAOException if a problem occurred during the request to the database or if we have wrong parameter
	 */
	public boolean isExerciseCategoryVoteForUserAndExercise(int userId, int exerciseId, int categoryId) throws DAOException{
		try{
			ExercisesCategoryVote exercisesCategoryVote = em.createNamedQuery("ExercisesCategoryVote.findByUserExerciseCategory", ExercisesCategoryVote.class)
															.setParameter("userId", userId)
															.setParameter("exerciseId", exerciseId)
															.setParameter("categoryId", categoryId)
															.getSingleResult();
			if(exercisesCategoryVote != null){
				return true;
			}
			return false;
		}catch(NoResultException e){
			return false;
		}catch(NonUniqueResultException e){
			return true;
		}catch(IllegalStateException  | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}
	
	/**
	 * Verify if the user can vote for the category of this exercise
	 * @param userId the user id
	 * @param language the language of the category
	 * @param category 
	 * @param exerciseId the id of the exercise
	 * @return a boolean, true if the user can vote otherwise false
	 * @throws DAOException if the information send in parameter are incorrect
	 */
	private boolean isUserCanVote(int userId, String language, Category category, int exerciseId) throws DAOException {
		Exercise exercise = exerciseDao.getExerciseById(exerciseId);
		if(category == null){
			throw new DAOException("60");
		}
		if(isExerciseCategoryVoteForUserAndExercise(userId, exercise.getId(), category.getId())){
			return false;
		}
		return true;
	}

	/**
	 * Delete all category votes, vote by the user send in parameter 
	 * @param userId The user Id
	 * @throws DAOException 
	 */
	public void deleteAllByUserId(Integer userId) throws DAOException {
		try{
			utx.begin();
			em.createQuery("DELETE FROM ExercisesCategoryVote e WHERE e.user.id = :userId").setParameter("userId", userId).executeUpdate();

			utx.commit();
		}catch(NotSupportedException | SecurityException | IllegalStateException | RollbackException | HeuristicMixedException
				| HeuristicRollbackException | SystemException e){
			try {
				utx.rollback();
				throw new DAOException("50");
			} catch (IllegalStateException | SecurityException | SystemException e1) {
				throw new DAOException("51");			
			}
		}
	}

}
