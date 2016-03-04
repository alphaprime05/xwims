package entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the exercises_levels_vote database table.
 * 
 */
@Entity
@Table(name="exercises_levels_vote")
@NamedQueries ({
	@NamedQuery(name="ExercisesLevelsVote.findAll", query="SELECT e FROM ExercisesLevelsVote e"),
	@NamedQuery(name="ExercisesLevelsVote.findByUser", query="DELETE FROM ExercisesLevelsVote e WHERE e.user.id = :userId")
	//TODO: REPLACE DELETE (WRONG NAME)
})

public class ExercisesLevelsVote implements Serializable {
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

	//bi-directional many-to-one association to Level
	@ManyToOne
	@JoinColumn(name="id_level", nullable = false)
	private Level level;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JoinColumn(name="id_user", nullable = false)
	private User user;

	public ExercisesLevelsVote() {
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

	public Level getLevel() {
		return this.level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}