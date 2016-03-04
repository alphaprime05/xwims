package dao;

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

import entity.Exercise;
import entity.ExercisesScoreVote;
import entity.User;
import exception.DAOException;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ExercisesScoreVoteDao extends AbstractGenericDao<ExercisesScoreVote, String>{

	@Resource
	private UserTransaction utx;
	
	@Inject
	private UserDao userDao;
	
	@Inject
	private ExerciseDao exerciseDao;
	
	public ExercisesScoreVoteDao() {
		super(ExercisesScoreVote.class);
	}
	
	/**
	 * Verify if the user already add score for the exercise 
	 * @param userId The user Id
	 * @param exerciseId The exercise Id
	 * @return a boolean true if the user already vote otherwise false
	 * @throws DAOException
	 */
	private boolean isExerciseScoreVoteForUserAndExercise(int userId, int exerciseId) throws DAOException {
		try{
			ExercisesScoreVote exercisesScoreVote = em.createNamedQuery("ExercisesScoreVote.findByExerciseAndUser", ExercisesScoreVote.class)
					 								  .setParameter("exerciseId", exerciseId)	   
													  .setParameter("userId", userId)
													  .getSingleResult();
			if(exercisesScoreVote != null){
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
	 * Get all scores attribute to the exercises
	 * @param exerciseId The exercise Id
	 * @return a list of Score attributed to the exercise 
	 * @throws DAOException if a problem occurred during the request to the database or if we have wrong parameter
	 */
	private List<ExercisesScoreVote> getExercisesScoreVoteByExerciseId(int exerciseId) throws DAOException {
		try{
			return em.createNamedQuery("ExercisesScoreVote.findByExercise", ExercisesScoreVote.class)
					 .setParameter("exerciseId", exerciseId)
					 .getResultList();
		}catch(IllegalStateException  | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}
	
	/**
	 * Verify if the user can add a score for the exercise
	 * @param userId the user id
	 * @param exercise
	 * @return a boolean if the user can add a score otherwise false
	 * @throws DAOException
	 */
	private boolean isUserCanVote(int userId, Exercise exercise) throws DAOException {
		if(isExerciseScoreVoteForUserAndExercise(userId, exercise.getId())){
			return false;
		}
		return true;
	}

	/**
	 * Add a score by the user for the exercise send in parameter
	 * @param userId The user Id
	 * @param exerciseId The exercise Id
	 * @param score the score to add
	 * @throws DAOException if the information send in parameter are incorrect or if the user can't vote
	 */
	public void addScoreForExercise(int userId, int exerciseId, double score) throws DAOException {
		User user = userDao.getUserById(userId);
		Exercise exercise = exerciseDao.getExerciseById(exerciseId);
		
		if(isUserCanVote(userId, exercise)){
			try{
				utx.begin();
				
				ExercisesScoreVote exercisesScoreVote = new ExercisesScoreVote();
				exercisesScoreVote.setExercise(exercise);
				exercisesScoreVote.setUser(user);
				exercisesScoreVote.setLevel(score);
				create(exercisesScoreVote);

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
	 * Get the score of the exercise send in parameter
	 * @param exerciseId The exercise Id
	 * @return the score of the exercise
	 * @throws DAOException if the information send in parameter are incorrect
	 */
	public double getScoreForExerciseOfExercise(Integer exerciseId) throws DAOException{
		List<ExercisesScoreVote> exercisesScoreVotes = getExercisesScoreVoteByExerciseId(exerciseId);
		double score = 0;
		for (ExercisesScoreVote exercisesScoreVote : exercisesScoreVotes) {
			score += exercisesScoreVote.getScore();
		}
		if(score==0 && exercisesScoreVotes.size()==0){
			return 0;
		}
		return score/exercisesScoreVotes.size();
	}
}
