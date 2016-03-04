package rep;

import java.util.ArrayList;
import java.util.List;

import entity.Category;

public class CategoryRep {
	private final Integer id;
	private final Boolean exercisesCanBeAttached;
	private final String wimsEnName;
	private final Integer parentCategoryId;
	private final List<Integer> subCategoriesId;
	
	public CategoryRep(Category category) {
		id = category.getId();
		if(category.getExercisesCanBeAttached() != null){
			exercisesCanBeAttached = category.getExercisesCanBeAttached();
		}else{
			exercisesCanBeAttached = true;
		}
		wimsEnName = category.getWimsEnName();
		if(category.getParentCategory() != null){
			parentCategoryId = category.getParentCategory().getId();
		}else{
			parentCategoryId = -1;
		}
		subCategoriesId = new ArrayList<>();
		if(category.getSubCategories() != null){
			for(Category c: category.getSubCategories()) {
				subCategoriesId.add(c.getId());
			}
		}
	}

	public Integer getId() {
		return id;
	}

	public Boolean getExercisesCanBeAttached() {
		return exercisesCanBeAttached;
	}

	public String getWimsEnName() {
		return wimsEnName;
	}

	public Integer getParentCategoryId() {
		return parentCategoryId;
	}

	public List<Integer> getSubCategoriesId() {
		return subCategoriesId;
	}
}
