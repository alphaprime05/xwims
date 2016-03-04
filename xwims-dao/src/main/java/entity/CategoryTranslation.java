package entity;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;


/**
 * The persistent class for the category_translation database table.
 * 
 */
@Entity
@Indexed
@Table(name="category_translation")
@NamedQueries ({
	@NamedQuery(name="CategoryTranslation.findAll", query="SELECT c FROM CategoryTranslation c"),
	@NamedQuery(name="CategoryTranslation.findById", query="SELECT c FROM CategoryTranslation c WHERE c.id = :id"),
	@NamedQuery(name="CategoryTranslation.findByName", query="SELECT c FROM CategoryTranslation c WHERE c.xwimsLanguage = :language AND c.xwimsTranslation = :name"),
	@NamedQuery(name="CategoryTranslation.findCategoryTranlationByCategoryId", query="SELECT c FROM CategoryTranslation c WHERE c.xwimsLanguage = :language AND c.category.id = :categoryId"),
	@NamedQuery(name="CategoryTranslation.findCategoryByTranslation", query="SELECT c FROM CategoryTranslation c WHERE c.xwimsTranslation = :translation AND c.xwimsLanguage = :lang"),
})

public class CategoryTranslation implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id",unique=true, nullable = false)
	private Integer id;

	@Column(name="xwims_language", nullable = false, length=5)
	private String xwimsLanguage;

	@Analyzer(definition = "searchtokenanalyzer")
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)
	@Column(name="xwims_translation", nullable = false)
	private String xwimsTranslation;

	//bi-directional many-to-one association to Category
	@ManyToOne
	@JoinColumn(name="id_category", nullable = false)
	private Category category;

	public CategoryTranslation() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getXwimsLanguage() {
		return this.xwimsLanguage;
	}

	public void setXwimsLanguage(String xwimsLanguage) {
		this.xwimsLanguage = xwimsLanguage;
	}

	public String getXwimsTranslation() {
		return this.xwimsTranslation;
	}

	public void setXwimsTranslation(String xwimsTranslation) {
		this.xwimsTranslation = xwimsTranslation;
	}

	public Category getCategory() {
		return this.category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	@Override
	public String toString() {
		return "CategoryTranslation [id=" + id + ", xwimsLanguage=" + xwimsLanguage + ", xwimsTranslation="
				+ xwimsTranslation + ", category=" + category.getId() + "]";
	}

	
}