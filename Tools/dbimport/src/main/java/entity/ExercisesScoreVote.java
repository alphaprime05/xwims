package entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the exercises_score_vote database table.
 * 
 */
@Entity
@Table(name="exercises_score_vote")
@NamedQueries ({
	@NamedQuery(name="ExercisesScoreVote.findAll", query="SELECT s FROM ExercisesScoreVote s"),
	@NamedQuery(name="ExercisesScoreVote.findByUser", query="SELECT s FROM ExercisesScoreVote s WHERE s.user.id = :userId"),
	@NamedQuery(name="ExercisesScoreVote.findByExerciseAndUser", query="SELECT s FROM ExercisesScoreVote s WHERE s.exercise.id = :exerciseId AND s.user.id = :userId"),
	@NamedQuery(name="ExercisesScoreVote.findByExercise", query="SELECT s FROM ExercisesScoreVote s WHERE s.exercise.id = :exerciseId")
})

public class ExercisesScoreVote implements Serializable {
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

    @Column(name = "score", nullable = false)
	private Double score;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JoinColumn(name="id_user", nullable = false)
	private User user;

	public ExercisesScoreVote() {
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

	public Double getScore() {
		return this.score;
	}

	public void setLevel(Double score) {
		this.score = score;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}