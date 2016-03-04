package com.esipe.dbimport;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import entity.Category;
import entity.CategoryTranslation;
import entity.Configuration;
import entity.Exercise;
import entity.ExerciseAuthor;
import entity.ExercisesKeywordsVote;
import entity.ExercisesLevelsVote;
import entity.Keyword;
import entity.KeywordTranslation;
import entity.Level;
import entity.User;

/**
 * DatabaseCollector is an object importing all the exercises and translations from wims to xWims
 * database.
 */
public class DatabaseCollector implements Closeable {

	private final EntityManagerFactory entityManagerFactory;
	private final EntityManager em;
	private final EntityTransaction tx;
	private final Path langFiles;
	private final Path exercisesFiles;
	private final Path levelFile;
	private final CharsetHandler handler;
	private final String wimsUrl;
	private final String serverUrl;
	private final String password;
	private User wims;


	/**
	 * Create a new DatabaseCollector
	 * @param pLevelFile 
	 * @param wimsUrl 
	 * @param wimsPassword 
	 * @param charsetMap A <String, String> containing the encoding system for each language. Exemple: "fr" -> "ASCII"
	 * @param wimsFolder The folder of the wims installation we want to import data from.
	 */
	public DatabaseCollector(final CharsetHandler pHandler, final Path pLangFiles, final Path pExercisesFiles, Path pLevelFile, String wimsUrl, String serverUrl, String wimsPassword) {
		this.wimsUrl = wimsUrl;
		this.serverUrl = serverUrl;
		this.handler = pHandler;
		this.langFiles = pLangFiles;
		this.levelFile = pLevelFile;
		this.exercisesFiles = pExercisesFiles;
		password = wimsPassword;
		entityManagerFactory = Persistence.createEntityManagerFactory("dbimport");
		em = entityManagerFactory.createEntityManager();
		tx = em.getTransaction();
	}

	/**
	 * Start importation from wims Database.
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	public void extractDatabase() throws NoSuchAlgorithmException, InvalidKeySpecException {
		try {
			System.out.println("IMPORTING CONF");
			addConf();
			System.out.println("CREATING XWIMS USER");
			addWimsUser();
			System.out.println("IMPORTING LEVELS");
			addWimsLevels();
			System.out.println("IMPORTING CATEGORIES");
			addCategories("Chimie", "fr", "Chemistry");
			addCategories("Art", "fr", "Art");
			addCategories("Instruction civique", "fr", "civic education");
			addCategories("Geographie", "fr", "Geography");
			addCategories("Histoire", "fr", "History");
			addCategories("Informatique", "fr", "Computer science");
			addCategories("Langage", "fr", "Language");
			addCategories("Physique", "fr", "Physics");
			addCategories("Sciences", "fr", "Sciences");
			addCategories("Mathématiques", "fr", "Mathematics");
			addCategories("Biologie", "fr", "Biology");
			System.out.println("IMPORTING KEYWORDS");
			HashMap<String, Keyword> keywordMap = new HashMap<>();
			LinkedList<KeywordTranslation> translationList = new LinkedList<>();
			searchForKeywords(keywordMap, translationList);
			tx.begin();
			try {
				for(Keyword k: keywordMap.values()) {
					em.persist(k);
				}
				em.flush();
				tx.commit();
			} catch(RollbackException | SecurityException | IllegalStateException e) {
				em.getTransaction().rollback();
				throw new IllegalStateException("Transaction error");
			}
			tx.begin();
			try {
				for(KeywordTranslation t: translationList) {
					em.persist(t);
				}
				em.flush();
				tx.commit();
			} catch(RollbackException | SecurityException | IllegalStateException e) {
				em.getTransaction().rollback();
			}

			System.out.println("IMPORTING EXERCISES");
			LinkedList<Exercise> exerciseList = new LinkedList<>();
			LinkedList<ExerciseAuthor> authorsList = new LinkedList<>();
			LinkedList<ExercisesKeywordsVote> exercisesKeywordsVoteList = new LinkedList<>();
			LinkedList<ExercisesLevelsVote> exercisesLevelsVoteList = new LinkedList<>();
			searchForExercises(exercisesFiles, exerciseList, authorsList, exercisesKeywordsVoteList, exercisesLevelsVoteList);
			tx.begin();
			try {
				for(Exercise e: exerciseList) {
					em.persist(e);
				}
				em.flush();
				tx.commit();
			} catch(RollbackException | SecurityException | IllegalStateException e) {
				em.getTransaction().rollback();
			}

			tx.begin();
			try {
				for(ExerciseAuthor e: authorsList) {
					em.persist(e);
				}
				for(ExercisesKeywordsVote e: exercisesKeywordsVoteList) {
					em.persist(e);
				}
				for(ExercisesLevelsVote e: exercisesLevelsVoteList) {
					em.persist(e);
				}
				em.flush();
				tx.commit();
			} catch(RollbackException | SecurityException | IllegalStateException e) {
				em.getTransaction().rollback();
			}
		} catch (IOException e) {
			System.err.println("ERROR: I/O Exception. Unable to read wims files. "
					+"Aborting...");
		}
	}

	private void addCategories(String name, String language, String enName) {
		tx.begin();
		Category cat = new Category();
		cat.setWimsEnName(enName);
		cat.setExercisesCanBeAttached(false);
		cat.setParentCategory(null);
		try {
			em.persist(cat);
			em.flush();
			tx.commit();
		} catch(RollbackException | SecurityException | IllegalStateException e) {
			em.getTransaction().rollback();
		}
		
		tx.begin();
		CategoryTranslation translation = new CategoryTranslation();
		translation.setCategory(cat);
		translation.setXwimsLanguage(language);
		translation.setXwimsTranslation(name);
		try {
			em.persist(translation);
			em.flush();
			tx.commit();
		} catch(RollbackException | SecurityException | IllegalStateException e) {
			em.getTransaction().rollback();
		}
	}
	
	private void addConf() {
		tx.begin();
		Configuration conf = new Configuration();
		conf.setServerUrl(serverUrl);
		conf.setWimsUrl(wimsUrl);
		try {
			em.persist(conf);
			em.flush();
			tx.commit();
		} catch(RollbackException | SecurityException | IllegalStateException e) {
			em.getTransaction().rollback();
		}
	}

	private void addWimsLevels() throws IOException {	
		tx.begin();
		Files.lines(levelFile, Charset.forName("iso-8859-1")).forEach(line -> {
			String tokens[] = line.split("=");
			if(tokens.length == 2 && tokens[0].equals("levellist")) {
				String levels[] = tokens[1].split(",");
				for(String l: levels) {
					Level level = new Level();
					level.setWimsName(l);
					em.persist(level);
					em.flush();
				}
			}
		});
		try {
			tx.commit();
		} catch(RollbackException | IllegalStateException e) {
			em.getTransaction().rollback();
		}

	}

	/**
	 * Add the xWims user into xWims database.
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	private void addWimsUser() throws NoSuchAlgorithmException, InvalidKeySpecException {
		tx.begin();
		wims = new User();
		HashedPassword p = new HashedPassword(password);
		wims.setFirstName("x");
		wims.setLastName("Wims");
		wims.setEmail("xwims@localhost");
		wims.setPasswordHash(p.getHash());
		wims.setPasswordSalt(p.getSalt());
		wims.setLanguage("fr");
		wims.setRegistrationDate(new Date());
		wims.setIsCertified(true);
		wims.setIsRoot(true);
		wims.setIsBanned(false);
		wims.setIsRegistered(true);
		wims.setRandomIdentifier(0);
		try {
			em.persist(wims);
			em.flush();
			tx.commit();
		} catch(RollbackException | SecurityException | IllegalStateException e) {
			em.getTransaction().rollback();
		}
	}

	/**
	 * Get a DirctoryStream containing all the files beginning with the specified prefix
	 * @param prefix The prefix looked for
	 * @return a DirctoryStream
	 * @throws IOException
	 */
	private DirectoryStream<Path> getDirectoryStreamStartingWith(String prefix) throws IOException {
		return Files.newDirectoryStream(langFiles, new DirectoryStream.Filter<Path>() {

			@Override
			public boolean accept(Path entry) throws IOException 
			{
				return entry.getFileName().toString().startsWith(prefix);
			}
		});
	}

	/**
	 * Search and add to the lists passed as parameters all the keywords and translations available
	 * @param translationList the translation list to fill
	 * @param keywordMap the keyword map to fill
	 * @throws IOException
	 */
	private void searchForKeywords(HashMap<String, Keyword> keywordMap, List<KeywordTranslation> translationList) throws IOException {
		HashSet<String> translationSet = new HashSet<>();
		try(DirectoryStream<Path> fileStream1 = getDirectoryStreamStartingWith("domaindic")) {


			/*
			 * As translations are not available for each keyword depending of the language, we have to scan all
			 * translation files for keywords.
			 */
			for (Path entry: fileStream1) {
				String fileName = entry.getFileName().toString();
				String fileLang = fileName.substring(fileName.length() -2, fileName.length());
				indexKeywords(entry, fileLang, translationSet, keywordMap);
			}

		}

		try(DirectoryStream<Path> fileStream2 = getDirectoryStreamStartingWith("domaindic")) {
			/*
			 * Then we scan for translations. 
			 */
			for (Path entry: fileStream2) {
				String fileName = entry.getFileName().toString();
				String fileLang = fileName.substring(fileName.length() -2, fileName.length());
				indexLanguage(entry, fileLang, translationList, keywordMap);
			}
		}
	}

	/**
	 * Search in the specified file for non-indexed keywords and add them to the list passed in parameter  
	 * @param entry The path to the required file
	 * @param lang The language of the required file
	 * @param translationSet A <String, String> containing the encoding system for each language. Exemple: "fr" -> "ASCII"
	 * @param keywordMap the map to fill
	 * @throws IOException
	 */
	private void indexKeywords(Path entry, String lang, HashSet<String> translationSet, HashMap<String, Keyword> keywordMap) throws IOException {
		Files.lines(entry, handler.getCharset(lang)).forEach(line -> {
			String[] tokens = line.split(":");

			if(tokens.length < 2) {
				return;
			}

			if(translationSet.contains(tokens[1])) {
				return;
			}

			Keyword keyword = new Keyword();
			keyword.setWimsEnName(tokens[1]);
			keywordMap.put(tokens[1], keyword);
			translationSet.add(tokens[1]);

		});
	}

	/**
	 * Reads the specified file looking for translations. Then adds them to the list passed as parameter.
	 * @param entry The path to the file
	 * @param lang The language of the file
	 * @param translationList the list to fill
	 * @param keywordMap 
	 * @throws IOException
	 */
	private void indexLanguage(Path entry, String lang, List<KeywordTranslation> translationList, HashMap<String, Keyword> keywordMap) throws IOException {
		Files.lines(entry, handler.getCharset(lang)).forEach(line -> {
			String[] tokens = line.split(":");
			Keyword k = keywordMap.get(tokens[1]);
			KeywordTranslation keywordTranslation = new KeywordTranslation();
			keywordTranslation.setXwimsLanguage(lang);
			keywordTranslation.setXwimsTranslation(tokens[0]);
			keywordTranslation.setKeyword(k);
			translationList.add(keywordTranslation);
		});
	}

	/**
	 * Scan all the directories looking for exercises and add them to the different passed in parameter
	 * @param scanDirectory The directory to scan
	 * @param exerciseList The exercise list to fill
	 * @param authorsList The authorExerciseList to fill
	 * @param exercisesKeywordsVoteList The exercisesKeywordsVote list to fill
	 * @param exercisesLevelsVoteList 
	 * @throws IOException
	 */
	private void searchForExercises(Path scanDirectory, List<Exercise> exerciseList, List<ExerciseAuthor> authorsList, List<ExercisesKeywordsVote> exercisesKeywordsVoteList, LinkedList<ExercisesLevelsVote> exercisesLevelsVoteList) throws IOException {
		DirectoryStream<Path> directoryStream = Files.newDirectoryStream(scanDirectory, new DirectoryStream.Filter<Path>() {

			@Override
			public boolean accept(Path entry) throws IOException 
			{
				return Files.isDirectory(entry);
			}
		});

		DirectoryStream<Path> fileStream = Files.newDirectoryStream(scanDirectory, new DirectoryStream.Filter<Path>() {

			@Override
			public boolean accept(Path entry) throws IOException 
			{
				return !Files.isDirectory(entry);
			}
		});

		Path src = scanDirectory.resolve("src");
		Path index = null;
		Path exauthors = null;
		Path extitles = null;
		Path exindex = null;

		for (Path entry: fileStream) {
			if(entry.getFileName().toString().equals("INDEX")) {
				index = entry;
			} else if(entry.getFileName().toString().equals("Exindex")) {
				exindex = entry;
			} else if(entry.getFileName().toString().equals("Extitles")) {
				extitles = entry;
			} else if(entry.getFileName().toString().equals("Exauthors")) {
				exauthors = entry;
			}
		}

		/* This condition is triggered when an OEF folder is found */
		if(src != null && index != null && exauthors != null && extitles != null && exindex != null) {
			HashMap<String, String> paramMap = new HashMap<>();
			Files.lines(index, Charset.forName("iso-8859-1")).forEach(line -> {
				String[] tokens = line.split("=");
				if(tokens.length < 2) {
					return;
				}
				paramMap.put(tokens[0], tokens[1]);
			});

			HashMap<String, String> exercisesMap = new HashMap<>();

			User xWims = em.createNamedQuery("User.findById", User.class).
					setParameter("id", 1).getSingleResult();

			Files.lines(extitles, Charset.forName("iso-8859-1")).forEach(line -> {
				String[] tokens = line.split(":");
				if(tokens.length < 2) {
					return;
				}
				exercisesMap.put(tokens[0], tokens[1]);
				Exercise exercise = new Exercise();
				String wimsIdentifier = scanDirectory.toString().substring(scanDirectory.getParent().getParent().getParent().toString().length() + 1);
				String levelName = wimsIdentifier.split("/")[0];
				String exerciseFileName = tokens[0];
				exercise.setWimsTitle(tokens[1]);
				exercise.setWimsExerciseFileName(exerciseFileName);
				exercise.setPopularity(0);
				exercise.setWimsAuthorEmail(paramMap.get("address"));
				exercise.setWimsLanguage(paramMap.get("language"));
				exercise.setWimsVersion(paramMap.get("version"));
				exercise.setWimsIdentifier(wimsIdentifier);

				try {
					Document doc = Jsoup.connect(wimsUrl + "wims.cgi?cmd=new&module="+ wimsIdentifier +"&exo="+ exerciseFileName +"&print=Version+imprimable").get();
					String content = doc.select("div.wimsbody").text();
					content = content.replaceAll("Your browser does not support the audio tag\\.", " ");
					int ind = content.indexOf("--- Printable version ---");
					if(ind > -1) {
						content = content.substring(ind);
					}
					ind = content.indexOf(")");
					if(ind > -1) {
						content = content.substring(ind);
					}
					ind = content.indexOf("Answers Answers");
					if(ind > -1) {
						content = content.substring(0, ind);
					}
					content = content.replaceAll("[^\\p{L}']", " ");
					content = content.trim().replaceAll(" +", " ");
					exercise.setWimsWording(content);
				} catch (Exception e1) {
					System.out.println("INFO: Unable to retrieve " + wimsIdentifier + " wording from wims."
							+ " If this message appears for all exercises, please check your wims URL.");
				}

				exerciseList.add(exercise);

				if(paramMap.containsKey("keywords")) {
					String keywords = paramMap.get("keywords");
					keywords = keywords.replace(" ", "");
					String[] keywordsToken = keywords.split(",");
					for(String keywordString: keywordsToken) {
						KeywordTranslation keywordTranslation;
						try {
							keywordTranslation = em.createNamedQuery("KeywordTranslation.findKeywordByTranslation", KeywordTranslation.class).
									setParameter("translation", keywordString).setParameter("language", paramMap.get("language")).getSingleResult();
						} catch(NoResultException e) {
							continue;
						}
						ExercisesKeywordsVote exercisesKeywordsVote = new ExercisesKeywordsVote();
						exercisesKeywordsVote.setExercise(exercise);
						exercisesKeywordsVote.setUser(xWims);
						exercisesKeywordsVote.setKeyword(keywordTranslation.getKeyword());
						exercisesKeywordsVoteList.add(exercisesKeywordsVote);
					}
				}

				Level level = null;
				try {
					level = em.createNamedQuery("Level.findLevelByName", Level.class).
							setParameter("wimsName", levelName).getSingleResult();
				} catch(NoResultException e) {
					System.out.println("Warning: Level error for exercise " + exerciseFileName + " in "+ wimsIdentifier);
				}

				if(level != null) {
					ExercisesLevelsVote exercisesLevelsVote = new ExercisesLevelsVote();
					exercisesLevelsVote.setExercise(exercise);
					exercisesLevelsVote.setUser(xWims);
					exercisesLevelsVote.setLevel(level);
					exercisesLevelsVoteList.add(exercisesLevelsVote);
				}

				if(paramMap.containsKey("author")) {
					String authors = paramMap.get("author");
					authors = authors.replace(" ", "");
					String[] authorsToken = authors.split(",");
					for(String authorString: authorsToken) {
						ExerciseAuthor author = new ExerciseAuthor();
						author.setAuthorName(authorString);
						author.setExercise(exercise);
						authorsList.add(author);
					}
				}

			});
			return;
		}
		for (Path entry: directoryStream) {
			if(Files.isDirectory(entry)) {
				searchForExercises(entry, exerciseList, authorsList, exercisesKeywordsVoteList, exercisesLevelsVoteList);
			} 
		}
	}

	@Override
	public void close() throws IOException {
		em.close();
		entityManagerFactory.close();
		System.out.println("IMPORTATION COMPLETE");
	}

	public void searchByDatabase(){
		List<Keyword> result = em.createNamedQuery("Keyword.findByName", Keyword.class)
				.setParameter("wimsEnName", "%plane%")
				.getResultList();

		System.out.println("size : "+result.size());
		for(Keyword exA : result){
			System.out.println(exA);
		}
	}
	public void indexFullDatabase(){
		EntityManager em = entityManagerFactory.createEntityManager();
		FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
		try {
			fullTextEntityManager.createIndexer().startAndWait();
		} catch (InterruptedException e) {
			System.out.println("ERROR: INDEXATION INTERRUPTED");
		}
	}
}
