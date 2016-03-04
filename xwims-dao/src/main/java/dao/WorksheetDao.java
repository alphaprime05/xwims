package dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import entity.User;
import entity.Worksheet;
import entity.WorksheetExercise;
import exception.DAOException;
import objects.WorksheetObject;
import rep.ExerciseRep;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class WorksheetDao extends AbstractGenericDao<Worksheet, String> {

	@Inject
	private ExerciseDao exerciseDao;

	@Inject
	private UserDao userDao;

	@Inject
	private WorksheetExerciseDao worksheetExerciseDao;

	@Inject
	private ExercisesScoreVoteDao exercisesScoreVoteDao;

	@Resource
	private UserTransaction utx;

	public WorksheetDao() {
		super(Worksheet.class);
	}

	/**
	 * This method gives details of a worksheet
	 * @param worksheet The worksheet to be detailed
	 * @return a WorksheetObject containing the information of the Worksheet sent in parameter
	 * @throws DAOException
	 */
	private WorksheetObject getExistingWorksheet(Worksheet worksheet) throws DAOException{
		WorksheetObject worksheetObject = new WorksheetObject();
		worksheetObject.setId(worksheet.getId());
		worksheetObject.setTitle(worksheet.getName());
		worksheetObject.setDescription(worksheet.getDescription());

		List<WorksheetExercise> worksheetExercises = new ArrayList<>();
		try {
			worksheetExercises = worksheetExerciseDao.getWorksheetExerciseByWorksheetId(worksheet.getId());
		} catch (DAOException e) {
			if(!e.getErrorCode().equals("6")){
				throw new DAOException(e.getErrorCode());
			}
		}
		List<Map<String, Object>> exercises_param = new ArrayList<>();
		for (WorksheetExercise worksheetExercise : worksheetExercises) {
			int position = worksheetExercise.getPosition();
			Exercise exercise = worksheetExercise.getExercise();
			Map<String, Object> exerciceParam = new HashMap<>();

			exerciceParam.put("popularity", exercise.getPopularity());
			exerciceParam.put("score", exercisesScoreVoteDao.getScoreForExerciseOfExercise(exercise.getId()));
			exerciceParam.put("position", position);
			exerciceParam.put("wims_identifier", exercise.getWimsIdentifier());
			exerciceParam.put("wims_exercise_name", exercise.getWimsExerciseFileName());
			exerciceParam.put("id_exercise", exercise.getId());

			exercises_param.add(exerciceParam);
		}
		worksheetObject.setExercices_param(exercises_param);
		return worksheetObject;
	}

	/**
	 * This method returns the last worksheet of an user
	 * @param userId The ID of the user we want to get the Worksheet
	 * @return a WorksheetObject containing the informations of the Last Worksheet
	 * @throws DAOException If the ID of the user is invalid
	 */
	public WorksheetObject getUserLastWorksheet(int userId) throws DAOException{
		List<Worksheet> worksheets = getWorksheetByUserId(userId);
		int lastElement = worksheets.size()-1;
		Worksheet worksheet = worksheets.get(lastElement);

		WorksheetObject worksheetObject = getExistingWorksheet(worksheet);

		return worksheetObject;
	}

	public List<WorksheetObject> getUserWorksheets(int userId) throws DAOException{
		List<WorksheetObject> worksheetObjects = new ArrayList<>();
		List<Worksheet> worksheets = getWorksheetByUserId(userId);

		for (Worksheet worksheet : worksheets) {
			worksheetObjects.add(getExistingWorksheet(worksheet));
		}

		return worksheetObjects;
	}

	/**
	 * This method save the worksheet in the database
	 * @param worksheet The worksheet that we want to save
	 * @return the id of The worsheet created
	 * @throws DAOException If the user is invalid or if the saving transaction in the database failed
	 */
	public int saveWorksheetInDatabase(WorksheetObject worksheet, int ownerId) throws DAOException {

		String title = worksheet.getTitle();
		String description = worksheet.getDescription();

		User user = userDao.getUserById(ownerId);

		try{
			utx.begin();

			Worksheet worksheetToSave = new Worksheet();
			worksheetToSave.setName(title);
			worksheetToSave.setDescription(description);
			worksheetToSave.setUser(user);
			worksheetToSave.setCreationDate(new Date());

			create(worksheetToSave);

			utx.commit();
			return worksheetToSave.getId();
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
	 * This method returns the code of the current Worksheet
	 * @return The generated code of the worksheet
	 * @throws DAOException 
	 */
	public String getCode(List<Map<String, Object>> exercises) throws DAOException {
		StringBuilder code = new StringBuilder();
		for (Map<String, Object> exerciseObject : exercises) {
			Exercise exercise = exerciseDao.getExerciseById((int) exerciseObject.get("id_exercise")); 
			code.append(":");
			code.append(exercise.getWimsIdentifier());
			code.append("\n");
			code.append("exo=");
			code.append(exercise.getWimsExerciseFileName());
			code.append("\n");
			code.append("10");
			code.append("\n");
			code.append("1");
			code.append("\n");
			code.append(exercise.getWimsTitle());
			code.append("\n");
		}
		return code.toString();
	}

	/**
	 * This method returns the list of worksheet owned by a user
	 * @param id The id of the user
	 * @return a worksheet list of the worksheet owned by the user
	 * @throws DAOException  If there is not Worksheet for this user
	 */
	private List<Worksheet> getWorksheetByUserId(int id) throws DAOException{
		List<Worksheet> worksheets;
		try{
			worksheets = em.createNamedQuery("Worksheet.findByOwnerId", Worksheet.class)
					.setParameter("id", id)
					.getResultList();
		}catch(IllegalStateException  | PersistenceException e){
			throw new DAOException("50");
		}
		if(!worksheets.isEmpty()){		
			return worksheets;
		}else{
			throw new DAOException("5");
		}
	}

	public Map<ExerciseRep, Integer> getCommonExercises(int id, String language) throws DAOException {
		List<Worksheet> worksheetList = em.createNamedQuery("Worksheet.findAll", Worksheet.class).getResultList();
		HashMap<ExerciseRep, Integer> resultMap = new HashMap<>();
		for(Worksheet w: worksheetList) {
			List<WorksheetExercise> worksheetExerciseList = w.getWorksheetExercises();
			boolean containsSeachedExercise = false;
			for(WorksheetExercise we: worksheetExerciseList) {
				if(we.getExercise().getId() == id) {
					containsSeachedExercise = true;
					break;
				}
			}
			if (containsSeachedExercise) {
				for(WorksheetExercise we: worksheetExerciseList) {
					int exerciseId = we.getExercise().getId();
					ExerciseRep rep = exerciseDao.getExerciseById(we.getExercise().getId(), language);
					if(exerciseId != id) {
						if(resultMap.containsKey(rep)) {
							resultMap.put(rep, resultMap.get(rep) + 1);
						} else {
							resultMap.put(rep, 1);
						}
					}
				}
			}
		}
		return resultMap;
	}

	/**
	 * Delete a worksheet by is id
	 * @param userId The user id
	 * @param worksheetId The worksheet Id
	 * @throws DAOException if a problem occurred during the delete transaction
	 */
	public void deleteByIdAndUserId(int userId, int worksheetId) throws DAOException{
		List<Worksheet> worksheets = getWorksheetByUserId(userId);
		boolean canDelete = false;
		for (Worksheet worksheet : worksheets) {
			if(worksheet.getId() == worksheetId){
				canDelete = true;
				break;
			}
		}
		if(canDelete){
			try {
				utx.begin();
				em.createQuery("DELETE FROM WorksheetExercise w WHERE w.worksheet.id = :id")
				  .setParameter("id", worksheetId)
				  .executeUpdate();
				
				em.createQuery("DELETE FROM Worksheet w WHERE w.user.id = :userId AND w.id = :id")
				  .setParameter("userId", userId)
				  .setParameter("id", worksheetId)
				  .executeUpdate();
				utx.commit();
			} catch (SecurityException | IllegalStateException | RollbackException | HeuristicMixedException
					| HeuristicRollbackException | SystemException | NotSupportedException e) {
				try {
					utx.rollback();
					throw new DAOException("50");
				} catch (IllegalStateException | SecurityException | SystemException e1) {
					throw new DAOException("51");
				}
			}
		}else{
			throw new DAOException("40");
		}
		
	}

	/**
	 * This method returns a worksheet by its ID
	 * @param id The id of the worksheet searched
	 * @return the selected workseet
	 * @throws DAOException If the worksheet ID is invalid
	 */
	public Worksheet getWorksheetById(int id) throws DAOException{
		try{
			return em.createNamedQuery("Worksheet.findById", Worksheet.class)
					.setParameter("id", id)
					.getSingleResult();
		}catch(NoResultException e){
			throw new DAOException("10");
		}catch(NonUniqueResultException e){
			throw new DAOException("14");
		}catch(IllegalStateException | PersistenceException e){
			throw new DAOException("50");
		}
	}
}
