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
import entity.Exercise;
import entity.PreviousCategoryForExercise;
import entity.User;
import exception.DAOException;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class PreviousCategoryForExerciseDao extends AbstractGenericDao<PreviousCategoryForExercise, String>{

	@Resource
	private UserTransaction utx;
	
	@Inject
	private ExerciseDao exerciseDao;
	
	@Inject
	private UserDao userDao;
	
	@Inject
	private CategoryTranslationDao categoryTranslationDao;
	

	public PreviousCategoryForExerciseDao() {
		super(PreviousCategoryForExercise.class);
	}

	/**
	 * Add a category to do before the exercise
	 * @param userId id of the user
	 * @param language of the user
	 * @param categoryTranslationId the category id
	 * @param exerciseId The exercise id
	 * @throws DAOException if a problem occurred during the creation or if the information send in parameter are incorrect
	 */
	public void addPreviousCategoryForExercise(int userId, String language, int categoryTranslationId, Integer exerciseId) throws DAOException {
		Category category = categoryTranslationDao.getCategoryTranslationById(categoryTranslationId).getCategory();
		User user = userDao.getUserById(userId);
		Exercise exercise = exerciseDao.getExerciseById(exerciseId);

		try{
			utx.begin();
			PreviousCategoryForExercise previousCategoryForExercise = new PreviousCategoryForExercise();
			previousCategoryForExercise.setCategory(category);
			previousCategoryForExercise.setExercise(exercise);
			previousCategoryForExercise.setUser(user);
			create(previousCategoryForExercise);
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

	/**
	 * Verify if the user already propose this previous category for the exercise
	 * @param userId The id of the user
	 * @param language The language of the category
	 * @param categoryTranslationId The id of the translation of the keyword
	 * @param exerciseId The exercise id
	 * @return a boolean, true if the user can add a previous category otherwise false
	 * @throws DAOException if the information send in parameter are incorrect
	 */
	public boolean isUserCanAddPreviousCategoryForExercise(int userId, String language, int categoryTranslationId, Integer exerciseId) throws DAOException {
		Exercise exercise = exerciseDao.getExerciseById(exerciseId);
		Category category = categoryTranslationDao.getCategoryTranslationById(categoryTranslationId).getCategory();
		if(isPreviousCategoryForExerciseExistForUser(userId, exercise.getId(), category.getId())){
			return false;
		}
		return true;
	}

	/**
	 * Verify if the user already proposed this previous category for the exercise
	 * @param userId The user Id
	 * @param exerciseId The exercise id
	 * @param categoryId The previous category id
	 * @return True if the user never proposed this category for this exercise
	 * @throws DAOException if a problem occurred during the request to the database or if we have wrong parameter
	 */ 
	public boolean isPreviousCategoryForExerciseExistForUser(int userId, int exerciseId, int categoryId) throws DAOException {
		try{
			PreviousCategoryForExercise previousCategoryForExercise = em.createNamedQuery("PreviousCategoryForExercise.findByUserCategoryExercise", PreviousCategoryForExercise.class)
																		.setParameter("userId", userId)
																		.setParameter("categoryId", categoryId)
																		.setParameter("exerciseId", exerciseId)
																		.getSingleResult();
			if(previousCategoryForExercise != null){
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
}
