package rep;

import entity.Level;

public class LevelRep {
	private final Integer id;
	private final String wimsName;

	public LevelRep(Level level) {
		id = level.getId();
		wimsName = level.getWimsName();
	}

	public Integer getId() {
		return id;
	}

	public String getWimsName() {
		return wimsName;
	}
}
