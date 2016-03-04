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
import entity.NextCategoryForExercise;
import entity.User;
import exception.DAOException;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class NextCategoryForExerciseDao extends AbstractGenericDao<NextCategoryForExercise, String>{

	@Resource
	private UserTransaction utx;

	@Inject
	private ExerciseDao exerciseDao;
	
	@Inject
	private UserDao userDao;
	
	@Inject
	private CategoryTranslationDao categoryTranslationDao;
	
	public NextCategoryForExerciseDao() {
		super(NextCategoryForExercise.class);
	}

	/**
	 * Add a vote for a category to do after the exercise
	 * @param userId The id of the user
	 * @param language The language of the user
	 * @param categoryTranslationId The id of the categorytranslation
	 * @param exerciseId The id of the exercise
	 * @throws DAOException
	 */
	public void addNextCategoryForExercise(int userId, String language, int categoryTranslationId, Integer exerciseId) throws DAOException {
		Category category = categoryTranslationDao.getCategoryTranslationById(categoryTranslationId).getCategory();
		User user = userDao.getUserById(userId);
		Exercise exercise = exerciseDao.getExerciseById(exerciseId);

		try{
			utx.begin();
			NextCategoryForExercise nextCategoryForExercise = new NextCategoryForExercise();
			nextCategoryForExercise.setCategory(category);
			nextCategoryForExercise.setExercise(exercise);
			nextCategoryForExercise.setUser(user);
			create(nextCategoryForExercise);
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
	 * Verify if the user already proposed this next category for the exercise
	 * @param userId The id of the user
	 * @param language The language of the user
	 * @param categoryTranslationId The id of the translation of the keyword
	 * @param exerciseId The id of the exercise
	 * @return True if the user never proposed this category for this exercise otherwise false
	 * @throws DAOException if the information send in parameter are incorrect
	 */
	public boolean isUserCanAddNextCategoryForExercise(int userId, String language, int categoryTranslationId, Integer exerciseId) throws DAOException {
		Exercise exercise = exerciseDao.getExerciseById(exerciseId);
		Category category = categoryTranslationDao.getCategoryTranslationById(categoryTranslationId).getCategory();
		if(isNextCategoryForExerciseExistForUser(userId, exercise.getId(), category.getId())){
			return false;
		}
		return true;
	}

	/**
	 * Verify if the user already proposed this next category for the exercise
	 * @param userId The id of the user
	 * @param exerciseId The id of the exercise
	 * @param categoryId The id of the of the keyword
	 * @return True if the user never proposed this category for this exercise
	 * @throws DAOException
	 */ 
	public boolean isNextCategoryForExerciseExistForUser(int userId, int exerciseId, int categoryId) throws DAOException {
		try{
			NextCategoryForExercise nextCategoryForExercise = em.createNamedQuery("NextCategoryForExercise.findByUserCategoryExercise", NextCategoryForExercise.class)
															.setParameter("userId", userId)
															.setParameter("categoryId", categoryId)
															.setParameter("exerciseId", exerciseId)
															.getSingleResult();
			if(nextCategoryForExercise != null){
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
