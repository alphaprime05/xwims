package entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the next_category_for_exercise database table.
 * 
 */
@Entity
@Table(name="next_category_for_exercise")
@NamedQueries ({
	@NamedQuery(name="NextCategoryForExercise.findAll", query="SELECT n FROM NextCategoryForExercise n"),
	@NamedQuery(name="NextCategoryForExercise.findByExerciseId", query="SELECT n FROM NextCategoryForExercise n WHERE n.exercise.id = :id"),
	@NamedQuery(name="NextCategoryForExercise.findByExerciseCategoryId", query="SELECT n FROM NextCategoryForExercise n WHERE n.exercise.id = :exerciseId AND n.category.id = :categoryId"),
	@NamedQuery(name="NextCategoryForExercise.findByUserCategoryExercise", query="SELECT n FROM NextCategoryForExercise n WHERE n.user.id = :userId AND n.category.id = :categoryId AND n.exercise.id = :exerciseId")
})
public class NextCategoryForExercise implements Serializable {
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

	public NextCategoryForExercise() {
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