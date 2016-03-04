package objects;

import java.util.List;
import java.util.Map;


public class WorksheetObject {
	
	private int id;
	private String title;
	private String description;
	private List<Map<String,Object>> exercices_param;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<Map<String, Object>> getExercices_param() {
		return exercices_param;
	}
	public void setExercices_param(List<Map<String, Object>> exercices_param) {
		this.exercices_param = exercices_param;
	}
	@Override
	public String toString() {
		return "WorksheetObject [id=" + id + ", title=" + title + ", description=" + description + ", exercices_param="
				+ exercices_param + "]";
	}
}
