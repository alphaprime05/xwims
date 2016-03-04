package entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the previous_category_for_exercise database table.
 * 
 */
@Entity
@Table(name="previous_category_for_exercise")
@NamedQueries ({
	@NamedQuery(name="PreviousCategoryForExercise.findAll", query="SELECT p FROM PreviousCategoryForExercise p"),
	@NamedQuery(name="PreviousCategoryForExercise.findByExerciseId", query="SELECT p FROM PreviousCategoryForExercise p WHERE p.exercise.id = :id"),
	@NamedQuery(name="PreviousCategoryForExercise.findByExerciseCategoryId", query="SELECT p FROM PreviousCategoryForExercise p WHERE p.exercise.id = :exerciseId AND p.category.id = :categoryId"),
	@NamedQuery(name="PreviousCategoryForExercise.findByUserCategoryExercise", query="SELECT p FROM PreviousCategoryForExercise p WHERE p.user.id = :userId AND p.category.id = :categoryId AND p.exercise.id = :exerciseId")
})
public class PreviousCategoryForExercise implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id",unique=true, nullable = false)
	private Integer id;

	//bi-directional many-to-one association to Category
	@ManyToOne
	@JoinColumn(name="id_category", nullable = false)
	private Category category;

	//bi-directional many-to-one association to Exercise
	@ManyToOne
	@JoinColumn(name="id_exercise", nullable = false)
	private Exercise exercise;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JoinColumn(name="id_user", nullable = false)
	private User user;

	public PreviousCategoryForExercise() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Category getCategory() {
		return this.category;
	}

	public void setCategory(Category category) {
		this.category = category;
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