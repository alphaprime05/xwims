package dao;
import java.util.ArrayList;
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
import entity.Exercise;
import entity.ExerciseIsWrong;
import entity.ExercisesCategoryVote;
import entity.ExercisesKeywordsVote;
import entity.Keyword;
import entity.KeywordTranslation;
import entity.NextCategoryForExercise;
import entity.PreviousCategoryForExercise;
import exception.DAOException;
import rep.CategoryTranslationRep;
import rep.ExerciseRep;
import rep.KeywordTranslationRep;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ExerciseDao extends AbstractGenericDao<Exercise, String> {

	@Resource
	private UserTransaction utx;
	
	@Inject
	private ExercisesCategoryVoteDao exercisesCategoryVoteDao;
	
	@Inject
	private ExercisesKeywordsVoteDao exercisesKeywordsVoteDao;
	
	@Inject
	private PreviousCategoryForExerciseDao previousCategoryForExerciseDao;
	
	@Inject
	private NextCategoryForExerciseDao nextCategoryForExerciseDao;
	
	@Inject
	private ExercisesScoreVoteDao exercisesScoreVoteDao;
	
	@Inject
	private ConfigurationDao confDao;
	
	public ExerciseDao() {
		super(Exercise.class);
	}

	/**
	 * Get an Exercise by is ID
	 * @param id of the Exercise
	 * @return The Exercise correspond to the id
	 * @throws DAOException, if the Exercise ID is invalid
	 */
	public Exercise getExerciseById(int id) throws DAOException {
		try{
			return em.createNamedQuery("Exercise.findByExerciseId", Exercise.class)
					.setParameter("id", id)
					.getSingleResult();
		}catch(NoResultException e){
			throw new DAOException("7");
		}catch(NonUniqueResultException e){
			return em.createNamedQuery("Exercise.findByExerciseId", Exercise.class)
					.setParameter("id", id)
					.getResultList().get(0);
		}catch(IllegalStateException | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}

	/**
	 * Get all exercises
	 * @param language The user language
	 * @return A list of all Exercises
	 * @throws DAOException if a problem occurred during the request to the database or if we have wrong parameter
	 */
	public List<ExerciseRep> findAllExercises(String language) throws DAOException {
		try{
			List<Exercise> list = em.createNamedQuery("Exercise.findAll", Exercise.class).getResultList();
			ArrayList<ExerciseRep> exerciseReps = new ArrayList<>();

			for(Exercise exercise: list) {
				exerciseReps.add(generateExerciseRep(language, exercise));
			}
			return exerciseReps;

		}catch(IllegalStateException | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}

	/**
	 * Generate a replication of exercise send in parameter
	 * @param language The user language
	 * @param exercise 
	 * @return a replication of exercise 
	 * @throws DAOException
	 */
	private ExerciseRep generateExerciseRep(String language, Exercise exercise) throws DAOException {
		return new ExerciseRep(exercise, confDao.getWimsUrl(), exercisesScoreVoteDao.getScoreForExerciseOfExercise(exercise.getId()));
	}

	/**
	 * Get limited list of exercise for pagination
	 * @param sizePage The number of exercise by page
	 * @param numberPage The page number we want to display
	 * @param language The user language
	 * @return a list of 15 exercises correspond 
	 * @throws DAOException if the we have a problem during the generation of the exercise replication 
	 */
	public List<ExerciseRep> findLimitedListExercise(int sizePage, int numberPage, String language) throws DAOException {
		if (numberPage <= 0) {
			return null;
		}
		ArrayList<ExerciseRep> exerciseReps = new ArrayList<>();

		try{
			List<Exercise> list = em.createNamedQuery("Exercise.findAll", Exercise.class)
					.setFirstResult((numberPage-1)*sizePage)
					.setMaxResults(sizePage-1)
					.getResultList();
			for(Exercise exercise: list) {
				exerciseReps.add(generateExerciseRep(language, exercise));
			}
			return exerciseReps;
		}catch(IllegalStateException | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}

	/**
	 * Search a sentence in the exercise title
	 * @param sentence The sentence searched in the title
	 * @param language of the title
	 * @return A list of exercise containing the sentence send in parameter
	 * @throws DAOException
	 */
	public List<ExerciseRep> searchInTitle(String sentence, String language) throws DAOException {
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
		QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Exercise.class).get();
		org.apache.lucene.search.Query query = qb.phrase().onField("wimsTitle").sentence(sentence).createQuery();

		javax.persistence.Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, Exercise.class);

		@SuppressWarnings("unchecked")
		List<Exercise> queryResult = persistenceQuery.getResultList();

		return toExerciseRepList(queryResult, language);
	}

	/**
	 * Search sentence in the exercise wording
	 * @param sentence The sentence searched in the wording
	 * @param language of the wording
	 * @return A list of exercise containing the sentence send in parameter
	 * @throws DAOException
	 */
	public List<ExerciseRep>  searchInWording(String sentence, String language) throws DAOException {
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
		QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Exercise.class).get();
		org.apache.lucene.search.Query query = qb.phrase().onField("wimsWording").sentence(sentence).createQuery();

		javax.persistence.Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, Exercise.class);

		@SuppressWarnings("unchecked")
		List<Exercise> queryResult = persistenceQuery.getResultList();

		return toExerciseRepList(queryResult, language);
	}

	/**
	 * Create a list of exercise replication with the exercise list
	 * @param exerciseList the list of exercise
	 * @param language of the user
	 * @return a list of exercise replication
	 * @throws DAOException if we have problem during the generation of exerciseRep
	 */
	private List<ExerciseRep> toExerciseRepList(List<Exercise> exerciseList, String language) throws DAOException {
		List<ExerciseRep> returnList = new ArrayList<>();
		for(Exercise e: exerciseList) {
			returnList.add(generateExerciseRep(language, e));
		}
		return returnList;
	}

	/**
	 * To know if the exercise send in parameter is wrong
	 * Consider an exercise is wrong when (warning alerts / number of users have been used this exercise) > 0.15
	 * @param exercise to verify if is wrong
	 * @return a boolean, if true the exercise send in parameter is wrong otherwise the exercise is valid
	 */
	public boolean isWrong(ExerciseRep exercise) {

		List<ExerciseIsWrong> exercicesIsWrong = em.createNamedQuery("ExerciseIsWrong.findByExerciseId", ExerciseIsWrong.class)
				.setParameter("exerciseId", exercise.getId())
				.getResultList();

		int popularity = exercise.getPopularity();
		int wrong_size = exercicesIsWrong.size();

		//to avoid a division per zero
		if (popularity <= 0 && wrong_size > 0) {
			return true;
		}

		if (popularity > 0 && (wrong_size / popularity) > 0.15) {
			return true;
		}

		return false;
	}

	
	/**
	 * Get the list of category votes for the exercise id send in parameter
	 * @param id of the exercise
	 * @return a list of category votes for a specific exercise
	 * @throws DAOException if a problem occurred during the request to the database or if we have wrong parameter
	 */
	private List<ExercisesCategoryVote> getCategoryByExerciseId(int id) throws DAOException{
		try{
			return em.createNamedQuery("ExercisesCategoryVote.findByExerciseId", ExercisesCategoryVote.class)
					.setParameter("id", id)
					.getResultList();
		}catch(IllegalStateException  | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}

	/**
	 * Get the list of keyword votes for the exercise id send in parameter
	 * @param id of the keyword
	 * @return a list of keyword votes for a specific exercise
	 * @throws DAOException if a problem occurred during the request to the database or if we have wrong parameter
	 */
	private List<ExercisesKeywordsVote> getKeywordByExerciseId(int id) throws DAOException{
		try{
			return em.createNamedQuery("ExercisesKeywordsVote.findByExerciseId", ExercisesKeywordsVote.class)
					.setParameter("exerciseId", id)
					.getResultList();
		}catch(IllegalStateException  | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}

	/**
	 * Get the list of next category votes for the exercise id send in parameter
	 * @param id of the next category
	 * @return a list of next category votes for a specific exercise
	 * @throws DAOException if a problem occurred during the request to the database or if we have wrong parameter
	 */
	private List<NextCategoryForExercise> getNextCategoryForExerciseByExerciseId(int id) throws DAOException{
		try{
			return em.createNamedQuery("NextCategoryForExercise.findByExerciseId", NextCategoryForExercise.class)
					.setParameter("id", id)
					.getResultList();
		}catch(IllegalStateException  | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}

	/**
	 * Get the list of previous category votes for the exercise id send in parameter
	 * @param id of the previous category
	 * @return a list of previous category votes for a specific exercise
	 * @throws DAOException if a problem occurred during the request to the database or if we have wrong parameter
	 */
	private List<PreviousCategoryForExercise> getPreviousCategoryForExerciseByExerciseId(int id) throws DAOException{
		try{
			return em.createNamedQuery("PreviousCategoryForExercise.findByExerciseId", PreviousCategoryForExercise.class)
					.setParameter("id", id)
					.getResultList();
		}catch(IllegalStateException  | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}

	/**
	 * Get the translation of the category send in parameter
	 * @param id of the category
	 * @param language of the user
	 * @return the translation of the category send in parameter
	 * @throws DAOException if no result for the id and language send in parameter or if a problem occurred during the request to the database or if we have wrong parameter
	 */
	private CategoryTranslation getCategoryTranslationByCategoryId(int id, String language) throws DAOException{
		try{
			return em.createNamedQuery("CategoryTranslation.findCategoryTranlationByCategoryId", CategoryTranslation.class)
					.setParameter("language", language)
					.setParameter("categoryId", id)
					.getSingleResult();
		}catch(NoResultException e){
			return null;
		}catch(NonUniqueResultException e){
			return em.createNamedQuery("CategoryTranslation.findCategoryTranlationByCategoryId", CategoryTranslation.class)
					.setParameter("language", language)
					.setParameter("categoryId", id)
					.getResultList().get(0);
		}catch(IllegalStateException | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}

	/**
	 * Get the translation of the keyword send in parameter
	 * @param id of the keyword
	 * @param language of the user
	 * @return the translation of the keyword send in parameter
	 * @throws DAOException if no result for the id and language send in parameter or if a problem occurred during the request to the database or if we have wrong parameter
	 */
	private KeywordTranslation getKeywordTranslationByCategoryId(int id, String language) throws DAOException{
		try{
			return em.createNamedQuery("KeywordTranslation.findKeywordTranlationByKeywordId", KeywordTranslation.class)
					.setParameter("language", language)
					.setParameter("keywordId", id)
					.getSingleResult();
		}catch(NoResultException e){
			return null;
		}catch(NonUniqueResultException e){
			return em.createNamedQuery("KeywordTranslation.findKeywordTranlationByKeywordId", KeywordTranslation.class)
					.setParameter("language", language)
					.setParameter("keywordId", id)
					.getResultList().get(0);
		}catch(IllegalStateException | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}

	/**
	 * Get the number of votes for a specific exercise and keyword
	 * @param exerciseId id of the exercise
	 * @param keywordId id of the keyword
	 * @return the number of votes
	 * @throws DAOException if a problem occurred during the request to the database or if we have wrong parameter
	 */
	private int getKeywordTranslationNbVoteByExerciseKeywordId(int exerciseId, int keywordId) throws DAOException{
		try{
			return em.createNamedQuery("ExercisesKeywordsVote.findByExerciseKeywordId", ExercisesKeywordsVote.class)
					.setParameter("exerciseId", exerciseId)
					.setParameter("keywordId", keywordId)
					.getResultList()
					.size();
		}catch(IllegalStateException | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}

	/**
	 * Get the number of votes for a specific exercise and category
	 * @param exerciseId id of the exercise
	 * @param categoryId id of the category
	 * @return the number of votes
	 * @throws DAOException if a problem occurred during the request to the database or if we have wrong parameter
	 */
	private int getCategoryTranslationNbVoteByExerciseCategoryId(int exerciseId, int categoryId) throws DAOException{
		try{
			return em.createNamedQuery("ExercisesCategoryVote.findByExerciseCategoryId", ExercisesCategoryVote.class)
					.setParameter("exerciseId", exerciseId)
					.setParameter("categoryId", categoryId)
					.getResultList()
					.size();
		}catch(IllegalStateException | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}

	/**
	 * Get the number of votes for a specific exercise and next category
	 * @param exerciseId id of the exercise
	 * @param categoryId id of the next category
	 * @return the number of votes
	 * @throws DAOException if a problem occurred during the request to the database or if we have wrong parameter
	 */
	private int getNextCategoryTranslationNbVoteForExerciseByExerciseCategoryId(int exerciseId, int categoryId) throws DAOException{
		try{
			return em.createNamedQuery("NextCategoryForExercise.findByExerciseCategoryId", NextCategoryForExercise.class)
					.setParameter("exerciseId", exerciseId)
					.setParameter("categoryId", categoryId)
					.getResultList()
					.size();
		}catch(IllegalStateException | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}

	/**
	 * Get the number of votes for a specific exercise and previous category
	 * @param exerciseId id of the exercise
	 * @param categoryId id of the previous category
	 * @return the number of votes
	 * @throws DAOException if a problem occurred during the request to the database or if we have wrong parameter
	 */
	private int getPreviousCategoryTranslationNbVoteForExerciseByExerciseCategoryId(int exerciseId, int categoryId) throws DAOException{
		try{
			return em.createNamedQuery("PreviousCategoryForExercise.findByExerciseCategoryId", PreviousCategoryForExercise.class)
					.setParameter("exerciseId", exerciseId)
					.setParameter("categoryId", categoryId)
					.getResultList()
					.size();
		}catch(IllegalStateException | PersistenceException e){
			throw new DAOException("50");
		}catch(IllegalArgumentException e){
			throw new DAOException("70");
		}
	}

	/**
	 * Generate a replication of exercise with the exercise id and language send in parameter
	 * @param exerciseId The id of the exercise
	 * @param language The user language
	 * @return a replication of exercise 
	 * @throws DAOException
	 */
	public ExerciseRep getExerciseById(int exerciseId, String language) throws DAOException {
		Exercise exercise = getExerciseById(exerciseId);
		return new ExerciseRep(exercise, confDao.getWimsUrl(), exercisesScoreVoteDao.getScoreForExerciseOfExercise(exerciseId));
	}

	/**
	 * Get a list of category to do before the exercise send in parameter
	 * @param userId the user Id
	 * @param exerciseId the exercise Id
	 * @param language the user language
	 * @return a list of category in user language to do before the exercise
	 * @throws DAOException if the information send in parameter are incorrect
	 */
	public List<CategoryTranslationRep> getPreviousCategoriesTranslationsForExerciseOfExercise(int userId, int exerciseId, String language) throws DAOException{
		Category category;
		CategoryTranslation categoryTranslation;
		CategoryTranslationRep categoryTranslationRep;

		List<CategoryTranslationRep> previousCategoryTranslationReps = new ArrayList<>();
		List<PreviousCategoryForExercise> previousCategoriesForExercises = getPreviousCategoryForExerciseByExerciseId(exerciseId);

		for (PreviousCategoryForExercise previousCategoryForExercise : previousCategoriesForExercises) {
			category = previousCategoryForExercise.getCategory();
			categoryTranslation = getCategoryTranslationByCategoryId(category.getId(), language);
			if(categoryTranslation != null){
				int nbVote = getPreviousCategoryTranslationNbVoteForExerciseByExerciseCategoryId(exerciseId, category.getId());
				boolean userVoted = previousCategoryForExerciseDao.isPreviousCategoryForExerciseExistForUser(userId, exerciseId, category.getId());
				categoryTranslationRep = new CategoryTranslationRep(categoryTranslation, nbVote, userVoted);
				if(!previousCategoryTranslationReps.contains(categoryTranslationRep)){
					previousCategoryTranslationReps.add(categoryTranslationRep);
				}
			}
		}
		return previousCategoryTranslationReps;
	}


	/**
	 * Get a list of category to do after the exercise send in parameter
	 * @param userId the user Id
	 * @param exerciseId the exercise Id
	 * @param language the user language
	 * @return a list of category in user language to do after the exercise
	 * @throws DAOException if the information send in parameter are incorrect
	 */
	public List<CategoryTranslationRep> getNextCategoriesTranslationsForExerciseOfExercise(int userId, int exerciseId, String language) throws DAOException{
		Category category;
		CategoryTranslation categoryTranslation;
		CategoryTranslationRep categoryTranslationRep;

		List<CategoryTranslationRep> nextCategoryTranslationReps = new ArrayList<>();
		List<NextCategoryForExercise> nextCategoriesForExercises = getNextCategoryForExerciseByExerciseId(exerciseId);

		for (NextCategoryForExercise nextCategoryForExercise : nextCategoriesForExercises) {
			category = nextCategoryForExercise.getCategory();
			categoryTranslation = getCategoryTranslationByCategoryId(category.getId(), language);
			if(categoryTranslation != null){
				int nbVote = getNextCategoryTranslationNbVoteForExerciseByExerciseCategoryId(exerciseId, category.getId());
				boolean userVoted = nextCategoryForExerciseDao.isNextCategoryForExerciseExistForUser(userId, exerciseId, category.getId());
				categoryTranslationRep = new CategoryTranslationRep(categoryTranslation, nbVote, userVoted);
				if(!nextCategoryTranslationReps.contains(categoryTranslationRep)){
					nextCategoryTranslationReps.add(categoryTranslationRep);
				}
			}
		}
		return nextCategoryTranslationReps;
	}

	/**
	 * Get a list of category associate to the exercise send in parameter
	 * @param userId the user Id
	 * @param exerciseId the exercise Id
	 * @param language the user language
	 * @return a list of category in user language associate to the exercise
	 * @throws DAOException if the information send in parameter are incorrect
	 */
	public List<CategoryTranslationRep> getCategoriesTranslationsOfExercise(int userId, int exerciseId, String language) throws DAOException{
		Category category;
		CategoryTranslation categoryTranslation;
		CategoryTranslationRep categoryTranslationRep;

		List<CategoryTranslationRep> categoryTranslationReps = new ArrayList<>();
		List<ExercisesCategoryVote> exerciseCategories = getCategoryByExerciseId(exerciseId);

		for (ExercisesCategoryVote exercisesCategoryVote : exerciseCategories) {
			category = exercisesCategoryVote.getCategory();
			categoryTranslation = getCategoryTranslationByCategoryId(category.getId(), language);
			if(categoryTranslation != null){
				int nbVote = getCategoryTranslationNbVoteByExerciseCategoryId(exerciseId, category.getId());
				boolean userVoted = exercisesCategoryVoteDao.isExerciseCategoryVoteForUserAndExercise(userId, exerciseId, category.getId());
				categoryTranslationRep = new CategoryTranslationRep(categoryTranslation, nbVote, userVoted);
				if(!categoryTranslationReps.contains(categoryTranslationRep)){
					categoryTranslationReps.add(categoryTranslationRep);
				}
			}
		}
		return categoryTranslationReps;
	}

	/**
	 * Get a list of keyword associate to the exercise send in parameter
	 * @param userId the user Id
	 * @param exerciseId the exercise Id
	 * @param language the user language
	 * @return a list of keyword in user language associate to the exercise
	 * @throws DAOException if the information send in parameter are incorrect
	 */
	public List<KeywordTranslationRep> getKeywordsTranslationsOfExercise(int userId, int exerciseId, String language) throws DAOException{

		Keyword keyword;
		KeywordTranslation keywordTranslation;
		KeywordTranslationRep keywordTranslationRep;

		List<KeywordTranslationRep> keywordTranslationReps = new ArrayList<>();
		List<ExercisesKeywordsVote> exercisesKeywordsVotes = getKeywordByExerciseId(exerciseId);
		for (ExercisesKeywordsVote exercisesKeywordsVote : exercisesKeywordsVotes) {
			keyword = exercisesKeywordsVote.getKeyword();
			keywordTranslation = getKeywordTranslationByCategoryId(keyword.getId(), language);
			if(keywordTranslation != null){
				int nbVote = getKeywordTranslationNbVoteByExerciseKeywordId(exerciseId, keyword.getId());
				boolean userVoted = exercisesKeywordsVoteDao.isExerciseKeywordVoteForUserAndExercise(userId, exerciseId, keyword.getId());
				keywordTranslationRep = new KeywordTranslationRep(keywordTranslation, nbVote, userVoted);
				if(!keywordTranslationReps.contains(keywordTranslationRep)){
					keywordTranslationReps.add(keywordTranslationRep);
				}
			}
		}
		return keywordTranslationReps;
	}

	/**
	 * Update the popularity of the exercise
	 * @param exercise the exercise we want to update popularity
	 * @throws DAOException if a problem occurred during the update in database
	 */
	public void updatePopularity(Exercise exercise) throws DAOException {
		try {
			utx.begin();
			
			exercise.setPopularity(exercise.getPopularity()+1);
			update(exercise);
			
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
}
