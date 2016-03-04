package rep;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ExerciseRep {
	private final int id;
	private final int popularity;
	private final double score;
	private final String wimeAuthorEmail;
	private final String wimsIdentifier;
	private final String wimsLanguage;
	private final String wimsTitle;
	private final String wimsVersion;
	private final String wimsWording;
	private boolean breakdown;
	private String viewUrl;
	private final String wimsFileName;
	
	public ExerciseRep(entity.Exercise ex, String wimsUrl, double score) {
		id = ex.getId();
		popularity = ex.getPopularity();
		this.score = score;
		wimeAuthorEmail = ex.getWimsAuthorEmail();
		wimsIdentifier = ex.getWimsIdentifier();
		wimsLanguage = ex.getWimsLanguage();
		wimsTitle = ex.getWimsTitle();
		wimsVersion = ex.getWimsVersion();
		wimsWording = ex.getWimsWording();
		wimsFileName = ex.getWimsExerciseFileName();
		try {
			viewUrl = wimsUrl + "wims.cgi?lang=fr&cmd=new&module=" + URLEncoder.encode(wimsIdentifier, "UTF-8") + "&exo="+URLEncoder.encode(wimsFileName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			viewUrl = null;
		}
	}

	public int getPopularity() {
		return popularity;
	}

	public double getScore() {
		return score;
	}

	public int getId() {
		return id;
	}

	public String getWimeAuthorEmail() {
		return wimeAuthorEmail;
	}

	public String getWimsIdentifier() {
		return wimsIdentifier;
	}

	public String getWimsLanguage() {
		return wimsLanguage;
	}

	public String getWimsTitle() {
		return wimsTitle;
	}

	public String getWimsVersion() {
		return wimsVersion;
	}

	public String getWimsWording() {
		return wimsWording;
	}

	public boolean isBreakdown() {
		return breakdown;
	}

	public void setBreakdown(boolean breakdown) {
		this.breakdown = breakdown;
	}

	public String getViewUrl() {
		return viewUrl;
	}

	public void setViewUrl(String viewUrl) {
		this.viewUrl = viewUrl;
	}

	public String getWimsFileName() {
		return wimsFileName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (breakdown ? 1231 : 1237);
		result = prime * result + id;
		result = prime * result + popularity;
		result = prime * result + ((viewUrl == null) ? 0 : viewUrl.hashCode());
		result = prime * result + ((wimeAuthorEmail == null) ? 0 : wimeAuthorEmail.hashCode());
		result = prime * result + ((wimsFileName == null) ? 0 : wimsFileName.hashCode());
		result = prime * result + ((wimsIdentifier == null) ? 0 : wimsIdentifier.hashCode());
		result = prime * result + ((wimsLanguage == null) ? 0 : wimsLanguage.hashCode());
		result = prime * result + ((wimsTitle == null) ? 0 : wimsTitle.hashCode());
		result = prime * result + ((wimsVersion == null) ? 0 : wimsVersion.hashCode());
		result = prime * result + ((wimsWording == null) ? 0 : wimsWording.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExerciseRep other = (ExerciseRep) obj;
		if (breakdown != other.breakdown)
			return false;
		if (id != other.id)
			return false;
		if (popularity != other.popularity)
			return false;
		if (viewUrl == null) {
			if (other.viewUrl != null)
				return false;
		} else if (!viewUrl.equals(other.viewUrl))
			return false;
		if (wimeAuthorEmail == null) {
			if (other.wimeAuthorEmail != null)
				return false;
		} else if (!wimeAuthorEmail.equals(other.wimeAuthorEmail))
			return false;
		if (wimsFileName == null) {
			if (other.wimsFileName != null)
				return false;
		} else if (!wimsFileName.equals(other.wimsFileName))
			return false;
		if (wimsIdentifier == null) {
			if (other.wimsIdentifier != null)
				return false;
		} else if (!wimsIdentifier.equals(other.wimsIdentifier))
			return false;
		if (wimsLanguage == null) {
			if (other.wimsLanguage != null)
				return false;
		} else if (!wimsLanguage.equals(other.wimsLanguage))
			return false;
		if (wimsTitle == null) {
			if (other.wimsTitle != null)
				return false;
		} else if (!wimsTitle.equals(other.wimsTitle))
			return false;
		if (wimsVersion == null) {
			if (other.wimsVersion != null)
				return false;
		} else if (!wimsVersion.equals(other.wimsVersion))
			return false;
		if (wimsWording == null) {
			if (other.wimsWording != null)
				return false;
		} else if (!wimsWording.equals(other.wimsWording))
			return false;
		return true;
	}

	
}
