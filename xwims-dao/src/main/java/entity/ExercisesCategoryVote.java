package entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the exercises_category_vote database table.
 * 
 */
@Entity
@Table(name="exercises_category_vote")
@NamedQueries ({
	@NamedQuery(name="ExercisesCategoryVote.findAll", query="SELECT e FROM ExercisesCategoryVote e"),
	@NamedQuery(name="ExercisesCategoryVote.findByUserExerciseCategory", query="SELECT e FROM ExercisesCategoryVote e WHERE e.user.id = :userId AND e.exercise.id = :exerciseId AND e.category.id = :categoryId"),
	@NamedQuery(name="ExercisesCategoryVote.findByExerciseId", query="SELECT e FROM ExercisesCategoryVote e WHERE e.exercise.id = :id"),
	@NamedQuery(name="ExercisesCategoryVote.findByExerciseCategoryId", query="SELECT e FROM ExercisesCategoryVote e WHERE e.exercise.id = :exerciseId AND e.category.id = :categoryId"),
	@NamedQuery(name="ExercisesCategoryVote.findByUser", query="SELECT e FROM ExercisesCategoryVote e WHERE e.user.id = :userId")
})
	
public class ExercisesCategoryVote implements Serializable {
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

	//bi-directional many-to-one association to Category
	@ManyToOne
	@JoinColumn(name="id_category", nullable = false)
	private Category category;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JoinColumn(name="id_user", nullable = false)
	private User user;

	public ExercisesCategoryVote() {
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

	public Category getCategory() {
		return this.category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}