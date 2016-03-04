package entity;

import java.io.Serializable;
import javax.persistence.*;

import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;

import java.util.List;


/**
 * The persistent class for the keyword database table.
 * 
 */
@Entity
@Indexed
@AnalyzerDef(name = "searchtokenanalyzer",
tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class ),
filters = {
		@TokenFilterDef(factory = StandardFilterFactory.class),
		@TokenFilterDef(factory = LowerCaseFilterFactory.class),
		@TokenFilterDef(factory = SnowballPorterFilterFactory.class, params = {@Parameter(name="language", value="French")}),
		@TokenFilterDef(factory = StopFilterFactory.class,
		params = {
	        @Parameter(name = "ignoreCase", value = "true")
	    })
})
@NamedQueries({
	@NamedQuery(name="Keyword.findAll", query="SELECT k FROM Keyword k"),
	@NamedQuery(name="Keyword.findById", query="SELECT k FROM Keyword k WHERE k.id = :id"),
	@NamedQuery(name="Keyword.findByName", query="SELECT k FROM Keyword k WHERE k.wimsEnName = :wimsEnName")
})
public class Keyword implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id",unique=true, nullable = false)
	private Integer id;

	@Analyzer(definition = "searchtokenanalyzer")
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.NO)
	@Column(name="wims_en_name", nullable = false)
	private String wimsEnName;

	//bi-directional many-to-one association to ExercisesKeywordsVote
	@OneToMany(mappedBy="keyword",orphanRemoval=true)
	private List<ExercisesKeywordsVote> exercisesKeywordsVotes;

	//bi-directional many-to-one association to KeywordTranslation
	@OneToMany(mappedBy="keyword",orphanRemoval=true)
	private List<KeywordTranslation> keywordTranslations;

	public Keyword() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getWimsEnName() {
		return this.wimsEnName;
	}

	public void setWimsEnName(String wimsEnName) {
		this.wimsEnName = wimsEnName;
	}

	public List<ExercisesKeywordsVote> getExercisesKeywordsVotes() {
		return this.exercisesKeywordsVotes;
	}

	public void setExercisesKeywordsVotes(List<ExercisesKeywordsVote> exercisesKeywordsVotes) {
		this.exercisesKeywordsVotes = exercisesKeywordsVotes;
	}

	public ExercisesKeywordsVote addExercisesKeywordsVote(ExercisesKeywordsVote exercisesKeywordsVote) {
		getExercisesKeywordsVotes().add(exercisesKeywordsVote);
		exercisesKeywordsVote.setKeyword(this);

		return exercisesKeywordsVote;
	}

	public ExercisesKeywordsVote removeExercisesKeywordsVote(ExercisesKeywordsVote exercisesKeywordsVote) {
		getExercisesKeywordsVotes().remove(exercisesKeywordsVote);
		exercisesKeywordsVote.setKeyword(null);

		return exercisesKeywordsVote;
	}

	public List<KeywordTranslation> getKeywordTranslations() {
		return this.keywordTranslations;
	}

	public void setKeywordTranslations(List<KeywordTranslation> keywordTranslations) {
		this.keywordTranslations = keywordTranslations;
	}

	public KeywordTranslation addKeywordTranslation(KeywordTranslation keywordTranslation) {
		getKeywordTranslations().add(keywordTranslation);
		keywordTranslation.setKeyword(this);

		return keywordTranslation;
	}

	public KeywordTranslation removeKeywordTranslation(KeywordTranslation keywordTranslation) {
		getKeywordTranslations().remove(keywordTranslation);
		keywordTranslation.setKeyword(null);

		return keywordTranslation;
	}

}