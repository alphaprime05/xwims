package entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;


/**
 * The persistent class for the levels database table.
 * 
 */
@Entity
@Indexed
@Table(name="levels")
@NamedQueries ({
	@NamedQuery(name="Level.findAll", query="SELECT c FROM Level c"),
	@NamedQuery(name = "Level.findLevelById", query = "SELECT c FROM Level c WHERE c.id = :id"),
	@NamedQuery(name = "Level.findLevelByName", query = "SELECT c FROM Level c WHERE c.wimsName = :wimsName"),
})
public class Level implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id",unique=true, nullable = false)
	private Integer id;

	@Analyzer(definition = "searchtokenanalyzer")
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)
	@Column(name="wims_name", nullable = false)
	private String wimsName;

	//bi-directional many-to-one association to ExercisesLevelsVote
	@OneToMany(mappedBy="level",orphanRemoval=true)
	private List<ExercisesLevelsVote> exercisesLevelsVotes;

	public Level() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getWimsName() {
		return this.wimsName;
	}

	public void setWimsName(String wimsName) {
		this.wimsName = wimsName;
	}

	public List<ExercisesLevelsVote> getExercisesLevelsVotes() {
		return this.exercisesLevelsVotes;
	}

	public void setExercisesLevelsVotes(List<ExercisesLevelsVote> exercisesLevelsVotes) {
		this.exercisesLevelsVotes = exercisesLevelsVotes;
	}

	public ExercisesLevelsVote addExercisesLevelsVote(ExercisesLevelsVote exercisesLevelsVote) {
		getExercisesLevelsVotes().add(exercisesLevelsVote);
		exercisesLevelsVote.setLevel(this);

		return exercisesLevelsVote;
	}

	public ExercisesLevelsVote removeExercisesLevelsVote(ExercisesLevelsVote exercisesLevelsVote) {
		getExercisesLevelsVotes().remove(exercisesLevelsVote);
		exercisesLevelsVote.setLevel(null);

		return exercisesLevelsVote;
	}
}