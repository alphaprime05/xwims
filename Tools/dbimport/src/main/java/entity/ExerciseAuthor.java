package entity;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;


/**
 * The persistent class for the exercise_author database table.
 * 
 */
@Entity
@Indexed
@Table(name="exercise_author")
@NamedQuery(name="ExerciseAuthor.findAll", query="SELECT e FROM ExerciseAuthor e")
public class ExerciseAuthor implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id",unique=true, nullable = false)
	private Integer id;

	@Analyzer(definition = "searchtokenanalyzer")
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)
	@Column(name="author_name", nullable=false)
	private String authorName;

	//bi-directional many-to-one association to Exercise
	@ManyToOne
	@JoinColumn(name="id_exercise", nullable = false)
	private Exercise exercise;

	public ExerciseAuthor() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAuthorName() {
		return this.authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public Exercise getExercise() {
		return this.exercise;
	}

	public void setExercise(Exercise exercise) {
		this.exercise = exercise;
	}

}