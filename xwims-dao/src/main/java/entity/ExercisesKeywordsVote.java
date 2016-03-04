package entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the exercises_keywords_vote database table.
 * 
 */
@Entity
@Table(name="exercises_keywords_vote")
@NamedQueries ({
	@NamedQuery(name="ExercisesKeywordsVote.findAll", query="SELECT e FROM ExercisesKeywordsVote e"),
	@NamedQuery(name="ExercisesKeywordsVote.findByUser", query="SELECT e FROM ExercisesKeywordsVote e WHERE e.user.id = :userId"),
	@NamedQuery(name="ExercisesKeywordsVote.findByExerciseId", query="SELECT e FROM ExercisesKeywordsVote e WHERE e.exercise.id = :exerciseId"),
	@NamedQuery(name="ExercisesKeywordsVote.findByExerciseKeywordId", query="SELECT e FROM ExercisesKeywordsVote e WHERE e.exercise.id = :exerciseId AND e.keyword.id = :keywordId"),
	@NamedQuery(name="ExercisesKeywordsVote.findByUserExerciseKeyword", query="SELECT e FROM ExercisesKeywordsVote e WHERE e.user.id = :userId AND e.exercise.id = :exerciseId AND e.keyword.id = :keywordId")
})
public class ExercisesKeywordsVote implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id",unique=true, nullable = false)
	private Integer id;

	//bi-directional many-to-one association to Exercise
	@ManyToOne
	@JoinColumn(name="id_exercise", nullable = false)
	private Exercise exercise;

	//bi-directional many-to-one association to Keyword
	@ManyToOne
	@JoinColumn(name="id_keyword", nullable = false)
	private Keyword keyword;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JoinColumn(name="id_user", nullable = false)
	private User user;

	public ExercisesKeywordsVote() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Exercise getExercise() {
		return this.exercise;
	}

	public void setExercise(Exercise exercise) {
		this.exercise = exercise;
	}

	public Keyword getKeyword() {
		return this.keyword;
	}

	public void setKeyword(Keyword keyword) {
		this.keyword = keyword;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}