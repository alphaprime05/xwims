package entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the user_selected_category database table.
 * 
 */
@Entity
@Table(name="user_selected_category")
@NamedQuery(name="UserSelectedCategory.findAll", query="SELECT u FROM UserSelectedCategory u")
public class UserSelectedCategory implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id",unique=true, nullable = false)
	private Integer id;

	//bi-directional many-to-one association to Category
	@ManyToOne
	@JoinColumn(name="id_category", nullable = false)
	private Category category;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JoinColumn(name="id_user", nullable = false)
	private User user;

	public UserSelectedCategory() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Category getCategory() {
		return this.category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}