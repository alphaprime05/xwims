package entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the exercise_is_wrong database table.
 */
@Entity
@Table(name="exercise_is_wrong")
@NamedQueries({
	@NamedQuery(name="ExerciseIsWrong.findAll", query="SELECT e FROM ExerciseIsWrong e"),
	@NamedQuery(name="ExerciseIsWrong.findByExerciseId", query="SELECT e FROM ExerciseIsWrong e WHERE e.exercise.id =:exerciseId")
})
public class ExerciseIsWrong implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id",unique=true, nullable = false)
	private Integer id;

	@Column(name="error_description", columnDefinition="text", nullable=false)
	private String errorDescription;

	//bi-directional many-to-one association to Exercise
	@ManyToOne
	@JoinColumn(name="id_exercise", nullable = false)
	private Exercise exercise;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JoinColumn(name="id_user", nullable = false)
	private User user;

	public ExerciseIsWrong() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getErrorDescription() {
		return this.errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	public Exercise getExercise() {
		return this.exercise;
	}

	public void setExercise(Exercise exercise) {
		this.exercise = exercise;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}