package entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;


/**
 * The persistent class for the worksheet database table.
 * 
 */
@Entity
@NamedQueries({
	@NamedQuery(name="Worksheet.findAll", query="SELECT w FROM Worksheet w"),
	@NamedQuery(name="Worksheet.findById", query="SELECT w FROM Worksheet w WHERE w.id = :id"),
	@NamedQuery(name="Worksheet.findByOwnerId", query="SELECT w FROM Worksheet w WHERE w.user.id = :id")
})
public class Worksheet implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id",unique=true, nullable = false)
	private Integer id;

	@Column(columnDefinition="text")
	private String description;

	@Column(nullable=false)
	private String name;
	
	//bi-directional many-to-one association to User
	@ManyToOne
	@JoinColumn(name="id_owner", nullable = false)
	private User user;

	//bi-directional many-to-one association to WorksheetExercis
	@OneToMany(mappedBy="worksheet",orphanRemoval=true)
	private List<WorksheetExercise> worksheetExercises;

	@Column(name="creation_date", nullable = false)
	private Date creationDate;
	
	public Worksheet() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public List<WorksheetExercise> getWorksheetExercises() {
		return this.worksheetExercises;
	}

	public void setWorksheetExercises(List<WorksheetExercise> worksheetExercises) {
		this.worksheetExercises = worksheetExercises;
	}

	public WorksheetExercise addWorksheetExercis(WorksheetExercise worksheetExercis) {
		getWorksheetExercises().add(worksheetExercis);
		worksheetExercis.setWorksheet(this);

		return worksheetExercis;
	}

	public WorksheetExercise removeWorksheetExercis(WorksheetExercise worksheetExercis) {
		getWorksheetExercises().remove(worksheetExercis);
		worksheetExercis.setWorksheet(null);

		return worksheetExercis;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date registrationDate) {
		this.creationDate = registrationDate;
	}
}