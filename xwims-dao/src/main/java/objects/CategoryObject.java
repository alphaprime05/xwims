package objects;

public class CategoryObject {
	
	private int id_exercise;
	private int found_step;
	private String category;
	private String category_en;
	private int id_parent_category;
	
	public int getId_exercise() {
		return id_exercise;
	}


	public void setId_exercise(int id_exercise) {
		this.id_exercise = id_exercise;
	}


	public int getFound_step() {
		return found_step;
	}


	public void setFound_step(int found_step) {
		this.found_step = found_step;
	}


	public String getCategory() {
		return category;
	}


	public void setCategory(String category) {
		this.category = category;
	}


	public String getCategory_en() {
		return category_en;
	}


	public void setCategory_en(String category_en) {
		this.category_en = category_en;
	}


	public int getId_parent_category() {
		return id_parent_category;
	}


	public void setId_parent_category(int id_parent_category) {
		this.id_parent_category = id_parent_category;
	}


	@Override
	public String toString() {
		return "CategoryObject [id_exercise=" + id_exercise + ", found_step=" + found_step + ", category=" + category
				+ ", category_en=" + category_en + ", id_parent_category=" + id_parent_category + "]";
	}
}
