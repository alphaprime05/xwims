package entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the worksheet_exercises database table.
 * 
 */
@Entity
@Table(name="worksheet_exercises")
@NamedQueries({
	@NamedQuery(name="WorksheetExercise.findAll", query="SELECT w FROM WorksheetExercise w"),
	@NamedQuery(name="WorksheetExercise.findByWorksheetId", query="SELECT w FROM WorksheetExercise w WHERE w.worksheet.id = :id")
	
})
public class WorksheetExercise implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id",unique=true, nullable = false)
	private Integer id;

	@Column(columnDefinition="Text")
	private String parameters;

	private Integer position;

	//bi-directional many-to-one association to Exercise
	@ManyToOne
	@JoinColumn(name="id_exercise", nullable = false)
	private Exercise exercise;

	//bi-directional many-to-one association to Worksheet
	@ManyToOne
	@JoinColumn(name="id_worksheet", nullable = false)
	private Worksheet worksheet;

	public WorksheetExercise() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getParameters() {
		return this.parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public Integer getPosition() {
		return this.position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public Exercise getExercise() {
		return this.exercise;
	}

	public void setExercise(Exercise exercise) {
		this.exercise = exercise;
	}

	public Worksheet getWorksheet() {
		return this.worksheet;
	}

	public void setWorksheet(Worksheet worksheet) {
		this.worksheet = worksheet;
	}

}