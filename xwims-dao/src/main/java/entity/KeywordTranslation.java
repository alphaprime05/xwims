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
 * The persistent class for the keyword_translation database table.
 * 
 */
@Entity
@Indexed
@Table(name="keyword_translation")
@NamedQueries ({
	@NamedQuery(name="KeywordTranslation.findAll", query="SELECT k FROM KeywordTranslation k"),
	@NamedQuery(name="KeywordTranslation.findKeywordTranlationByKeywordId", query="SELECT k FROM KeywordTranslation k WHERE k.xwimsLanguage=:language AND k.keyword.id = :keywordId"),
	@NamedQuery(name="KeywordTranslation.findKeywordByTranslation", query="SELECT k FROM KeywordTranslation k WHERE k.xwimsTranslation=:translation AND k.xwimsLanguage=:language")
})

public class KeywordTranslation implements Serializable {
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

	//bi-directional many-to-one association to Keyword
	@ManyToOne
	@JoinColumn(name="id_keyword", nullable = false)
	private Keyword keyword;

	public KeywordTranslation() {
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

	public Keyword getKeyword() {
		return this.keyword;
	}

	public void setKeyword(Keyword keyword) {
		this.keyword = keyword;
	}

}