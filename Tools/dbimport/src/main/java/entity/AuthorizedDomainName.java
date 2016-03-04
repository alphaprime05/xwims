package entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the authorized_domain_names database table.
 * 
 */
@Entity
@Table(name="authorized_domain_names")
@NamedQuery(name="AuthorizedDomainName.findAll", query="SELECT a FROM AuthorizedDomainName a")
public class AuthorizedDomainName implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id",unique=true, nullable = false)
	private Integer id;

	@Column(columnDefinition="text", nullable=false)
	private String domain;

	public AuthorizedDomainName() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDomain() {
		return this.domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

}