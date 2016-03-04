package dao;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import entity.ExercisesLevelsVote;
import exception.DAOException;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ExercisesLevelsVoteDao extends AbstractGenericDao<ExercisesLevelsVote, String>{

	@Resource
	private UserTransaction utx;

	public ExercisesLevelsVoteDao() {
		super(ExercisesLevelsVote.class);
	}

	/**
	 * Delete level votes by Id 
	 * @param id The ExercisesLevelsVote Id
	 * @throws DAOException 
	 */
	public void deleteById(int id) throws DAOException {
		try{
			utx.begin();
			em.createQuery("DELETE FROM ExercisesLevelsVote e WHERE e.id = :id").setParameter("id", id).executeUpdate();

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

	/**
	 * Delete all level votes, vote by the user send in parameter
	 * @param userId The user Id
	 * @throws DAOException 
	 */
	public void deleteAllByUserId(Integer userId) throws DAOException {
		try{
			utx.begin();
			em.createQuery("DELETE FROM ExercisesLevelsVote e WHERE e.user.id = :userId").setParameter("userId", userId).executeUpdate();

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