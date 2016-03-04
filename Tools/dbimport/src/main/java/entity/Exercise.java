package entity;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.NumericField;
import org.hibernate.search.annotations.Store;

import java.util.List;


/**
 * The persistent class for the exercise database table.
 * 
 */
@Entity
@Indexed
@NamedQueries({
	@NamedQuery(name="Exercise.findAll", query="SELECT e FROM Exercise e"),
	@NamedQuery(name="Exercise.findByExerciseId", query="SELECT e FROM Exercise e WHERE e.id = :id")
})
public class Exercise implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id",unique=true, nullable = false)
	private Integer id;

	@NumericField
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)
	@Column(nullable=false)
	private Integer popularity;

	@Column(name="wims_author_email")
	private String wimsAuthorEmail;

	@Column(name="wims_exercise_file_name", nullable=false)
	private String wimsExerciseFileName;

	@Analyzer(definition = "searchtokenanalyzer")
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)
	@Column(name="wims_identifier", nullable=false)
	private String wimsIdentifier;

	@Analyzer(definition = "searchtokenanalyzer")
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)
	@Column(name="wims_language")
	private String wimsLanguage;

	@Analyzer(definition = "searchtokenanalyzer")
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)
	@Column(name="wims_title", nullable=false)
	private String wimsTitle;

	@Column(name="wims_version")
	private String wimsVersion;
	
	@Analyzer(definition = "searchtokenanalyzer")
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)
	@Column(name="wims_wording", columnDefinition="text")
	private String wimsWording;

	//bi-directional many-to-one association to ExerciseAuthor
	@OneToMany(mappedBy="exercise")
	private List<ExerciseAuthor> exerciseAuthors;

	//bi-directional many-to-one association to ExerciseComment
	@OneToMany(mappedBy="exercise")
	private List<ExerciseComment> exerciseComments;

	//bi-directional many-to-one association to ExerciseIsWrong
	@OneToMany(mappedBy="exercise")
	private List<ExerciseIsWrong> exerciseIsWrongs;

	//bi-directional many-to-one association to ExercisesCategoryVote
	@OneToMany(mappedBy="exercise")
	private List<ExercisesCategoryVote> exercisesCategoryVotes;

	//bi-directional many-to-one association to ExercisesKeywordsVote
	@OneToMany(mappedBy="exercise")
	private List<ExercisesKeywordsVote> exercisesKeywordsVotes;

	//bi-directional many-to-one association to ExercisesLevelsVote
	@OneToMany(mappedBy="exercise")
	private List<ExercisesLevelsVote> exercisesLevelsVotes;

	//bi-directional many-to-one association to NextCategoryForExercise
	@OneToMany(mappedBy="exercise")
	private List<NextCategoryForExercise> nextCategoryForExercises;

	//bi-directional many-to-one association to PreviousCategoryForExercise
	@OneToMany(mappedBy="exercise")
	private List<PreviousCategoryForExercise> previousCategoryForExercises;

	//bi-directional many-to-one association to WorksheetExercis
	@OneToMany(mappedBy="exercise")
	private List<WorksheetExercise> worksheetExercises;

	public Exercise() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getPopularity() {
		return this.popularity;
	}

	public void setPopularity(Integer popularity) {
		this.popularity = popularity;
	}

	public String getWimsAuthorEmail() {
		return this.wimsAuthorEmail;
	}

	public void setWimsAuthorEmail(String wimsAuthorEmail) {
		this.wimsAuthorEmail = wimsAuthorEmail;
	}

	public String getWimsExerciseFileName() {
		return this.wimsExerciseFileName;
	}

	public void setWimsExerciseFileName(String wimsExerciseFileName) {
		this.wimsExerciseFileName = wimsExerciseFileName;
	}

	public String getWimsIdentifier() {
		return this.wimsIdentifier;
	}

	public void setWimsIdentifier(String wimsIdentifier) {
		this.wimsIdentifier = wimsIdentifier;
	}

	public String getWimsLanguage() {
		return this.wimsLanguage;
	}

	public void setWimsLanguage(String wimsLanguage) {
		this.wimsLanguage = wimsLanguage;
	}

	public String getWimsTitle() {
		return this.wimsTitle;
	}

	public void setWimsTitle(String wimsTitle) {
		this.wimsTitle = wimsTitle;
	}

	public String getWimsVersion() {
		return this.wimsVersion;
	}

	public void setWimsVersion(String wimsVersion) {
		this.wimsVersion = wimsVersion;
	}
	
	public String getWimsWording() {
		return this.wimsWording;
	}

	public void setWimsWording(String wimsWording) {
		this.wimsWording = wimsWording;
	}

	public List<ExerciseAuthor> getExerciseAuthors() {
		return this.exerciseAuthors;
	}

	public void setExerciseAuthors(List<ExerciseAuthor> exerciseAuthors) {
		this.exerciseAuthors = exerciseAuthors;
	}

	public ExerciseAuthor addExerciseAuthor(ExerciseAuthor exerciseAuthor) {
		getExerciseAuthors().add(exerciseAuthor);
		exerciseAuthor.setExercise(this);

		return exerciseAuthor;
	}

	public ExerciseAuthor removeExerciseAuthor(ExerciseAuthor exerciseAuthor) {
		getExerciseAuthors().remove(exerciseAuthor);
		exerciseAuthor.setExercise(null);

		return exerciseAuthor;
	}

	public List<ExerciseComment> getExerciseComments() {
		return this.exerciseComments;
	}

	public void setExerciseComments(List<ExerciseComment> exerciseComments) {
		this.exerciseComments = exerciseComments;
	}

	public ExerciseComment addExerciseComment(ExerciseComment exerciseComment) {
		getExerciseComments().add(exerciseComment);
		exerciseComment.setExercise(this);

		return exerciseComment;
	}

	public ExerciseComment removeExerciseComment(ExerciseComment exerciseComment) {
		getExerciseComments().remove(exerciseComment);
		exerciseComment.setExercise(null);

		return exerciseComment;
	}

	public List<ExerciseIsWrong> getExerciseIsWrongs() {
		return this.exerciseIsWrongs;
	}

	public void setExerciseIsWrongs(List<ExerciseIsWrong> exerciseIsWrongs) {
		this.exerciseIsWrongs = exerciseIsWrongs;
	}

	public ExerciseIsWrong addExerciseIsWrong(ExerciseIsWrong exerciseIsWrong) {
		getExerciseIsWrongs().add(exerciseIsWrong);
		exerciseIsWrong.setExercise(this);

		return exerciseIsWrong;
	}

	public ExerciseIsWrong removeExerciseIsWrong(ExerciseIsWrong exerciseIsWrong) {
		getExerciseIsWrongs().remove(exerciseIsWrong);
		exerciseIsWrong.setExercise(null);

		return exerciseIsWrong;
	}

	public List<ExercisesCategoryVote> getExercisesCategoryVotes() {
		return this.exercisesCategoryVotes;
	}

	public void setExercisesCategoryVotes(List<ExercisesCategoryVote> exercisesCategoryVotes) {
		this.exercisesCategoryVotes = exercisesCategoryVotes;
	}

	public ExercisesCategoryVote addExercisesCategoryVote(ExercisesCategoryVote exercisesCategoryVote) {
		getExercisesCategoryVotes().add(exercisesCategoryVote);
		exercisesCategoryVote.setExercise(this);

		return exercisesCategoryVote;
	}

	public ExercisesCategoryVote removeExercisesCategoryVote(ExercisesCategoryVote exercisesCategoryVote) {
		getExercisesCategoryVotes().remove(exercisesCategoryVote);
		exercisesCategoryVote.setExercise(null);

		return exercisesCategoryVote;
	}

	public List<ExercisesKeywordsVote> getExercisesKeywordsVotes() {
		return this.exercisesKeywordsVotes;
	}

	public void setExercisesKeywordsVotes(List<ExercisesKeywordsVote> exercisesKeywordsVotes) {
		this.exercisesKeywordsVotes = exercisesKeywordsVotes;
	}

	public ExercisesKeywordsVote addExercisesKeywordsVote(ExercisesKeywordsVote exercisesKeywordsVote) {
		getExercisesKeywordsVotes().add(exercisesKeywordsVote);
		exercisesKeywordsVote.setExercise(this);

		return exercisesKeywordsVote;
	}

	public ExercisesKeywordsVote removeExercisesKeywordsVote(ExercisesKeywordsVote exercisesKeywordsVote) {
		getExercisesKeywordsVotes().remove(exercisesKeywordsVote);
		exercisesKeywordsVote.setExercise(null);

		return exercisesKeywordsVote;
	}

	public List<ExercisesLevelsVote> getExercisesLevelsVotes() {
		return this.exercisesLevelsVotes;
	}

	public void setExercisesLevelsVotes(List<ExercisesLevelsVote> exercisesLevelsVotes) {
		this.exercisesLevelsVotes = exercisesLevelsVotes;
	}

	public ExercisesLevelsVote addExercisesLevelsVote(ExercisesLevelsVote exercisesLevelsVote) {
		getExercisesLevelsVotes().add(exercisesLevelsVote);
		exercisesLevelsVote.setExercise(this);

		return exercisesLevelsVote;
	}

	public ExercisesLevelsVote removeExercisesLevelsVote(ExercisesLevelsVote exercisesLevelsVote) {
		getExercisesLevelsVotes().remove(exercisesLevelsVote);
		exercisesLevelsVote.setExercise(null);

		return exercisesLevelsVote;
	}

	public List<NextCategoryForExercise> getNextCategoryForExercises() {
		return this.nextCategoryForExercises;
	}

	public void setNextCategoryForExercises(List<NextCategoryForExercise> nextCategoryForExercises) {
		this.nextCategoryForExercises = nextCategoryForExercises;
	}

	public NextCategoryForExercise addNextCategoryForExercis(NextCategoryForExercise nextCategoryForExercis) {
		getNextCategoryForExercises().add(nextCategoryForExercis);
		nextCategoryForExercis.setExercise(this);

		return nextCategoryForExercis;
	}

	public NextCategoryForExercise removeNextCategoryForExercis(NextCategoryForExercise nextCategoryForExercis) {
		getNextCategoryForExercises().remove(nextCategoryForExercis);
		nextCategoryForExercis.setExercise(null);

		return nextCategoryForExercis;
	}

	public List<PreviousCategoryForExercise> getPreviousCategoryForExercises() {
		return this.previousCategoryForExercises;
	}

	public void setPreviousCategoryForExercises(List<PreviousCategoryForExercise> previousCategoryForExercises) {
		this.previousCategoryForExercises = previousCategoryForExercises;
	}

	public PreviousCategoryForExercise addPreviousCategoryForExercis(PreviousCategoryForExercise previousCategoryForExercis) {
		getPreviousCategoryForExercises().add(previousCategoryForExercis);
		previousCategoryForExercis.setExercise(this);

		return previousCategoryForExercis;
	}

	public PreviousCategoryForExercise removePreviousCategoryForExercis(PreviousCategoryForExercise previousCategoryForExercis) {
		getPreviousCategoryForExercises().remove(previousCategoryForExercis);
		previousCategoryForExercis.setExercise(null);

		return previousCategoryForExercis;
	}

	public List<WorksheetExercise> getWorksheetExercises() {
		return this.worksheetExercises;
	}

	public void setWorksheetExercises(List<WorksheetExercise> worksheetExercises) {
		this.worksheetExercises = worksheetExercises;
	}

	public WorksheetExercise addWorksheetExercis(WorksheetExercise worksheetExercis) {
		getWorksheetExercises().add(worksheetExercis);
		worksheetExercis.setExercise(this);

		return worksheetExercis;
	}

	public WorksheetExercise removeWorksheetExercis(WorksheetExercise worksheetExercis) {
		getWorksheetExercises().remove(worksheetExercis);
		worksheetExercis.setExercise(null);

		return worksheetExercis;
	}

	@Override
	public String toString() {
		return "Exercise [id=" + id + ", popularity=" + popularity + ", wimsAuthorEmail="
				+ wimsAuthorEmail + ", wimsExerciseFileName=" + wimsExerciseFileName + ", wimsIdentifier="
				+ wimsIdentifier + ", wimsLanguage=" + wimsLanguage + ", wimsTitle=" + wimsTitle + ", wimsVersion="
				+ wimsVersion + ", wimsWording=" + wimsWording + "]\n\n";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Exercise other = (Exercise) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (popularity == null) {
			if (other.popularity != null)
				return false;
		} else if (!popularity.equals(other.popularity))
			return false;
		if (wimsAuthorEmail == null) {
			if (other.wimsAuthorEmail != null)
				return false;
		} else if (!wimsAuthorEmail.equals(other.wimsAuthorEmail))
			return false;
		if (wimsExerciseFileName == null) {
			if (other.wimsExerciseFileName != null)
				return false;
		} else if (!wimsExerciseFileName.equals(other.wimsExerciseFileName))
			return false;
		if (wimsIdentifier == null) {
			if (other.wimsIdentifier != null)
				return false;
		} else if (!wimsIdentifier.equals(other.wimsIdentifier))
			return false;
		if (wimsLanguage == null) {
			if (other.wimsLanguage != null)
				return false;
		} else if (!wimsLanguage.equals(other.wimsLanguage))
			return false;
		if (wimsTitle == null) {
			if (other.wimsTitle != null)
				return false;
		} else if (!wimsTitle.equals(other.wimsTitle))
			return false;
		if (wimsVersion == null) {
			if (other.wimsVersion != null)
				return false;
		} else if (!wimsVersion.equals(other.wimsVersion))
			return false;
		if (wimsWording == null) {
			if (other.wimsWording != null)
				return false;
		} else if (!wimsWording.equals(other.wimsWording))
			return false;
		return true;
	}

}
