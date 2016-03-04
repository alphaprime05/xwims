package entity;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


/**
 * The persistent class for the users database table.
 * 
 */
@Entity
@Table(name="configuration")
@NamedQueries({
	@NamedQuery(name="Configuration.findAll", query="SELECT u FROM Configuration u"),
})
public class Configuration implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id",unique=true, nullable = false)
	private Integer id;

	@Column(nullable=false)
	private String serverUrl;
	
	@Column(nullable=false)
	private String wimsUrl;

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getWimsUrl() {
		return wimsUrl;
	}

	public void setWimsUrl(String wimsUrl) {
		this.wimsUrl = wimsUrl;
	}

	public Integer getId() {
		return id;
	}

}