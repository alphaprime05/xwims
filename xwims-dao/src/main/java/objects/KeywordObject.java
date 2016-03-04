package objects;

public class KeywordObject {
	
	private int id_exercise;
	private int found_step;
	private String keyword;
	private String keyword_en;
	
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


	public String getKeyword() {
		return keyword;
	}


	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}


	public String getKeyword_en() {
		return keyword_en;
	}


	public void setKeyword_en(String keyword_en) {
		this.keyword_en = keyword_en;
	}


	@Override
	public String toString() {
		return "KeywordObject [id_exercise=" + id_exercise + ", found_step=" + found_step + ", keyword=" + keyword
				+ ", keyword_en=" + keyword_en + "]";
	}
}
