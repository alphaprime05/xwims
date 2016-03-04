package rep;

import entity.CategoryTranslation;

public class CategoryTranslationRep {
	
	private int id;
	private String xwims_translation;
	private int nbVote;
	private boolean hasUserVoted;
	
	public CategoryTranslationRep(CategoryTranslation categoryTranslation, int nbVote, boolean hasUserVoted) {
		this.id = categoryTranslation.getId();
		this.xwims_translation = categoryTranslation.getXwimsTranslation();
		this.nbVote = nbVote;
		this.hasUserVoted = hasUserVoted;
	}
	
	public CategoryTranslationRep(CategoryTranslation translation) {
		id = translation.getId();
		xwims_translation = translation.getXwimsTranslation();
	}
	
	public CategoryTranslationRep() {
	}
	
	public int getId() {
		return id;
	}

	public String getXwims_translation() {
		return xwims_translation;
	}
	
	public int getNbVote() {
		return nbVote;
	}

	public boolean isHasUserVoted() {
		return hasUserVoted;
	}

	@Override
	public String toString() {
		return "CategoryTranslationRep [id=" + id + ", xwims_translation=" + xwims_translation + ", nbVote=" + nbVote
				+ ", hasUserVoted=" + hasUserVoted + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (hasUserVoted ? 1231 : 1237);
		result = prime * result + id;
		result = prime * result + nbVote;
		result = prime * result + ((xwims_translation == null) ? 0 : xwims_translation.hashCode());
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
		CategoryTranslationRep other = (CategoryTranslationRep) obj;
		if (hasUserVoted != other.hasUserVoted)
			return false;
		if (id != other.id)
			return false;
		if (nbVote != other.nbVote)
			return false;
		if (xwims_translation == null) {
			if (other.xwims_translation != null)
				return false;
		} else if (!xwims_translation.equals(other.xwims_translation))
			return false;
		return true;
	}
}
