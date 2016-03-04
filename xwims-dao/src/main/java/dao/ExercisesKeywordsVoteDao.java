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

import entity.Exercise;
import entity.ExercisesKeywordsVote;
import entity.Keyword;
import entity.KeywordTranslation;
import entity.User;
import exception.DAOException;
import objects.KeywordObject;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ExercisesKeywordsVoteDao extends AbstractGenericDao<ExercisesKeywordsVote, String> {

	@Resource
	private UserTransaction utx;
	
	@Inject
	private UserDao userDao;
	
	@Inject
	private KeywordTranslationDao keywordTranslationDao;
	
	@Inject
	private ExerciseDao exerciseDao;
	
	@Inject
	private KeywordDao keywordDao;
	
	public ExercisesKeywordsVoteDao() {
		super(ExercisesKeywordsVote.class);
	}

	/**
	 * Add vote for a keyword for the exercise send in parameter by the keywordObject
	 * @param userId The user Id
	 * @param language The language of the keyword
	 * @param keywordObject have the information like the keyword to add, the exercise concerned, the keyword translation
	 * @throws DAOException if the information send in parameter are incorrect
	 */
	public void addKeywordVote(int userId, String language, KeywordObject keywordObject) throws DAOException {
		User user = userDao.getUserById(userId);
		Exercise exercise = exerciseDao.getExerciseById(keywordObject.getId_exercise());
		int foundStep = keywordObject.getFound_step();
		Keyword keyword;
		if(foundStep == 1){
			keyword = keywordDao.getKeywordByNameAndLanguage(language, keywordObject.getKeyword());
		}else if(foundStep == 2){
			keyword = keywordDao.getKeywordByName(keywordObject.getKeyword_en());
			if(isExistKeywordTranslation(language, keyword.getId())){
				throw new DAOException("20");
			}
			keywordTranslationDao.createKeywordTranslation(keywordObject.getKeyword(), language, keyword);
		}else if(foundStep == 3){
			keyword = keywordDao.getKeywordByName(keywordObject.getKeyword_en());
		}else{
			throw new DAOException("62");
		}

		if(isUserCanVote(userId, language, keyword, exercise.getId())){
			try{
				utx.begin();
				ExercisesKeywordsVote exercisesKewordsVote = new ExercisesKeywordsVote();
				exercisesKewordsVote.setKeyword(keyword);
				exercisesKewordsVote.setExercise(exercise);
				exercisesKewordsVote.setUser(user);

				create(exercisesKewordsVote);

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
	 * Verify if the user already vote for the keyword for the exercise
	 * @param userId The user Id
	 * @param exerciseId The exercise Id
	 * @param keywordId The keyword Id
	 * @return a boolean, true if the user already vote otherwise false
	 * @throws DAOException if a problem occurred during the request to the database or if we have wrong parameter
	 */
	public boolean isExerciseKeywordVoteForUserAndExercise(int userId, int exerciseId, int keywordId) throws DAOException {
		try{
			ExercisesKeywordsVote exercisesKeywordVote = em.createNamedQuery("ExercisesKeywordsVote.findByUserExerciseKeyword", ExercisesKeywordsVote.class)
														   .setParameter("userId", userId)
														   .setParameter("exerciseId", exerciseId)
														   .setParameter("keywordId", keywordId)
														   .getSingleResult();
			if(exercisesKeywordVote != null){
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
	 * Verify if the user can vote for the keyword of this exercise
	 * @param userId the user id
	 * @param language the language of the keyword
	 * @param keyword
	 * @param exerciseId the id of the exercise
	 * @return a boolean, true if the user can vote otherwise false
	 * @throws DAOException if the information send in parameter are incorrect
	 */
	private boolean isUserCanVote(int userId, String language, Keyword keyword, int exerciseId) throws DAOException {
		Exercise exercise = exerciseDao.getExerciseById(exerciseId);
		if(keyword == null){
			throw new DAOException("60");
		}
		if(isExerciseKeywordVoteForUserAndExercise(userId, exercise.getId(), keyword.getId())){
			return false;
		}
		return true;
	}

	/**
	 * Verify if the translation of the keyword already exist
	 * @param language The translation language
	 * @param keywordId The keyword Id
	 * @return a boolean, true if the translation exist otherwise false
	 * @throws DAOException if a problem occurred during the request to the database or if we have wrong parameter
	 */
	private boolean isExistKeywordTranslation(String language, int keywordId) throws DAOException{
		try{
			KeywordTranslation keywordTranslation = em.createNamedQuery("KeywordTranslation.findKeywordTranlationByKeywordId", KeywordTranslation.class)
					.setParameter("language", language)
					.setParameter("keywordId", keywordId)
					.getSingleResult();
			if(keywordTranslation != null){
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
	 * Delete all keyword votes, vote by the user send in parameter 
	 * @param userId The user Id
	 * @throws DAOException 
	 */
	public void deleteAllByUserId(Integer userId) throws DAOException {
		try{
			utx.begin();
			em.createQuery("DELETE FROM ExercisesKeywordsVote e WHERE e.user.id = :userId").setParameter("userId", userId).executeUpdate();

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
