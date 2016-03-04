package dao;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import entity.Exercise;
import entity.Worksheet;
import entity.WorksheetExercise;
import exception.DAOException;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class WorksheetExerciseDao extends AbstractGenericDao<WorksheetExercise, String> {

	@Resource
	private UserTransaction utx;
	
	@Inject
	private ExerciseDao exerciseDao;
	
	@Inject
	private WorksheetDao worksheetDao;

	public WorksheetExerciseDao() {
		super(WorksheetExercise.class);
	}

	/**
	 * This method saves the information of the WorksheetExercise in the database, the correspondence between the Worksheet and each exercise
	 * @param worksheetExercises The list of the exercise information
	 * @param worksheetId The id of the Worksheet
	 * @throws DAOException If the saving transaction in the database has failed
	 */
	public void saveWorksheetExerciseInDatabase(List<Map<String, Object>> worksheetExercises, int worksheetId) throws DAOException {
		try {
			utx.begin();
			Worksheet worksheet = worksheetDao.getWorksheetById(worksheetId);
			int position = 0;
			for(Map<String, Object> map : worksheetExercises){
				position++;
				WorksheetExercise worksheetExercise = new WorksheetExercise();
				Exercise exercise = exerciseDao.getExerciseById((int) map.get("id_exercise"));
				
				exerciseDao.updatePopularity(exercise);
				
				worksheetExercise.setExercise(exercise);
				worksheetExercise.setPosition(position);
				worksheetExercise.setWorksheet(worksheet);
				
				create(worksheetExercise);
			}
			utx.commit();

		} catch(NotSupportedException | SecurityException | IllegalStateException | RollbackException | HeuristicMixedException
				| HeuristicRollbackException | SystemException e){
			try {
				utx.rollback();
			} catch (IllegalStateException | SecurityException | SystemException e1) {
				throw new DAOException("9");
			}
			throw new DAOException("9");
		} catch (DAOException e) {
			try {
				utx.rollback();
			} catch (IllegalStateException | SecurityException | SystemException e1) {
				throw new DAOException(e.getErrorCode());
			}
			throw new DAOException(e.getErrorCode());
		}
	}
	
	/**
	 * This method generates the list of the exercise present in a worksheet, and their position in the worksheet
	 * @param id The id of the worksheet
	 * @return A list of WorksheetExercice (position, exercise)
	 * @throws DAOException If there is not exercices in the worksheet
	 */
	public List<WorksheetExercise> getWorksheetExerciseByWorksheetId(int id) throws DAOException{
		List<WorksheetExercise> worksheetExercises;
		try{
			worksheetExercises = em.createNamedQuery("WorksheetExercise.findByWorksheetId", WorksheetExercise.class)
					.setParameter("id", id)
					.getResultList();
		}catch(IllegalStateException  | PersistenceException e){
			throw new DAOException("50");
		}

	if(!worksheetExercises.isEmpty()){
			return worksheetExercises;
		}else{
			throw new DAOException("6");
		}
	}
}
