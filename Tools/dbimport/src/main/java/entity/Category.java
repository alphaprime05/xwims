package entity;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.List;


/**
 * The persistent class for the category database table.
 * 
 */
@Entity
@Indexed
@NamedQueries ({
		@NamedQuery(name="Category.findAll", query="SELECT c FROM Category c"),
		@NamedQuery(name="Category.findCategoryById", query="SELECT c FROM Category c WHERE c.id = :id"),
		@NamedQuery(name="Category.findCategoryByName", query="SELECT c FROM Category c WHERE c.wimsEnName = :name"),
		@NamedQuery(name="Category.findTopCategories", query="SELECT c FROM Category c WHERE c.parentCategory IS NULL"),
		@NamedQuery(name="Category.findAttachableCategories", query="SELECT c FROM Category c WHERE c.exercisesCanBeAttached = TRUE"),
		@NamedQuery(name="Category.findTopCategoriesLimited", query="SELECT c.wimsEnName, c.id FROM Category c WHERE c.parentCategory IS NULL")
})
public class Category implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id",unique=true, nullable = false)
	private Integer id;

	@Column(name="exercises_can_be_attached",columnDefinition = "boolean default true")
	private Boolean exercisesCanBeAttached = true;

	@Analyzer(definition = "searchtokenanalyzer")
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)
	@Column(name="wims_en_name", nullable = false)
	private String wimsEnName;

	@ManyToOne
	@JsonBackReference
	private Category parentCategory;
	
	@OneToMany(mappedBy = "parentCategory",orphanRemoval=true)
	@JsonManagedReference
	private List<Category> subCategories;
	
	//bi-directional many-to-one association to ExercisesCategoryVote
	@OneToMany(mappedBy="category",orphanRemoval=true)
	private List<ExercisesCategoryVote> exercisesCategoryVotes;

	//bi-directional many-to-one association to CategoryTranslation
	@OneToMany(mappedBy="category",orphanRemoval=true)
	private List<CategoryTranslation> categoryTranslations;

	//bi-directional many-to-one association to NextCategoryForExercise
	@OneToMany(mappedBy="category",orphanRemoval=true)
	private List<NextCategoryForExercise> nextCategoryForExercises;

	//bi-directional many-to-one association to PreviousCategoryForExercise
	@OneToMany(mappedBy="category",orphanRemoval=true)
	private List<PreviousCategoryForExercise> previousCategoryForExercises;

	//bi-directional many-to-one association to UserSelectedCategory
	@OneToMany(mappedBy="category",orphanRemoval=true)
	private List<UserSelectedCategory> userSelectedCategories;

	public Category() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Boolean getExercisesCanBeAttached() {
		return this.exercisesCanBeAttached;
	}

	public void setExercisesCanBeAttached(Boolean exercisesCanBeAttached) {
		this.exercisesCanBeAttached = exercisesCanBeAttached;
	}

	public String getWimsEnName() {
		return this.wimsEnName;
	}

	public void setWimsEnName(String wimsEnName) {
		this.wimsEnName = wimsEnName;
	}

	public Category getParentCategory() {
		return parentCategory;
	}

	public void setParentCategory(Category parentCategory) {
		this.parentCategory = parentCategory;
	}

	public List<Category> getSubCategories() {
		return subCategories;
	}

	public void setSubCategories(List<Category> subCategories) {
		this.subCategories = subCategories;
	}

	public List<CategoryTranslation> getCategoryTranslations() {
		return this.categoryTranslations;
	}

	public void setCategoryTranslations(List<CategoryTranslation> categoryTranslations) {
		this.categoryTranslations = categoryTranslations;
	}
	
	public List<ExercisesCategoryVote> getExercisesCategoryVotes() {
		return this.exercisesCategoryVotes;
	}

	public void setExercisesCategoryVotes(List<ExercisesCategoryVote> exercisesCategoryVotes) {
		this.exercisesCategoryVotes = exercisesCategoryVotes;
	}

	public ExercisesCategoryVote addExercisesCategoryVote(ExercisesCategoryVote exercisesCategoryVote) {
		getExercisesCategoryVotes().add(exercisesCategoryVote);
		exercisesCategoryVote.setCategory(this);

		return exercisesCategoryVote;
	}

	public ExercisesCategoryVote removeExercisesCategoryVote(ExercisesCategoryVote exercisesCategoryVote) {
		getExercisesCategoryVotes().remove(exercisesCategoryVote);
		exercisesCategoryVote.setCategory(null);

		return exercisesCategoryVote;
	}

	public CategoryTranslation addCategoryTranslation(CategoryTranslation categoryTranslation) {
		getCategoryTranslations().add(categoryTranslation);
		categoryTranslation.setCategory(this);

		return categoryTranslation;
	}

	public CategoryTranslation removeCategoryTranslation(CategoryTranslation categoryTranslation) {
		getCategoryTranslations().remove(categoryTranslation);
		categoryTranslation.setCategory(null);

		return categoryTranslation;
	}

	public List<NextCategoryForExercise> getNextCategoryForExercises() {
		return this.nextCategoryForExercises;
	}

	public void setNextCategoryForExercises(List<NextCategoryForExercise> nextCategoryForExercises) {
		this.nextCategoryForExercises = nextCategoryForExercises;
	}

	public NextCategoryForExercise addNextCategoryForExercis(NextCategoryForExercise nextCategoryForExercis) {
		getNextCategoryForExercises().add(nextCategoryForExercis);
		nextCategoryForExercis.setCategory(this);

		return nextCategoryForExercis;
	}

	public NextCategoryForExercise removeNextCategoryForExercis(NextCategoryForExercise nextCategoryForExercis) {
		getNextCategoryForExercises().remove(nextCategoryForExercis);
		nextCategoryForExercis.setCategory(null);

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
		previousCategoryForExercis.setCategory(this);

		return previousCategoryForExercis;
	}

	public PreviousCategoryForExercise removePreviousCategoryForExercis(PreviousCategoryForExercise previousCategoryForExercis) {
		getPreviousCategoryForExercises().remove(previousCategoryForExercis);
		previousCategoryForExercis.setCategory(null);

		return previousCategoryForExercis;
	}

	public List<UserSelectedCategory> getUserSelectedCategories() {
		return this.userSelectedCategories;
	}

	public void setUserSelectedCategories(List<UserSelectedCategory> userSelectedCategories) {
		this.userSelectedCategories = userSelectedCategories;
	}

	public UserSelectedCategory addUserSelectedCategory(UserSelectedCategory userSelectedCategory) {
		getUserSelectedCategories().add(userSelectedCategory);
		userSelectedCategory.setCategory(this);

		return userSelectedCategory;
	}

	public UserSelectedCategory removeUserSelectedCategory(UserSelectedCategory userSelectedCategory) {
		getUserSelectedCategories().remove(userSelectedCategory);
		userSelectedCategory.setCategory(null);

		return userSelectedCategory;
	}

	@Override
	public String toString() {
		return "Category [id=" + id + ", exercisesCanBeAttached=" + exercisesCanBeAttached + ", wimsEnName="
				+ wimsEnName + "]";
	}
	
}
