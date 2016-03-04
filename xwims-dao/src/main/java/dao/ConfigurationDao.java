package dao;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.UserTransaction;

import entity.Configuration;
import exception.DAOException;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ConfigurationDao extends AbstractGenericDao<Configuration, String> {
	@Resource
	private UserTransaction utx;

	public ConfigurationDao() {
		super(Configuration.class);
	}

	/**
	 * Get the the URL of the server
	 * @return A string containing the URL
	 * @throws DAOException
	 */
	public String getServerUrl() throws DAOException {
		 List<Configuration> configurations = em.createNamedQuery("Configuration.findAll", Configuration.class).getResultList();
		 if(configurations.isEmpty()) {
			 throw new DAOException("90");
		 }
		 
		 return configurations.get(0).getServerUrl();
	}
	
	/**
	 * Get the the URL of Wims
	 * @return A string containing the URL
	 * @throws DAOException
	 */
	public String getWimsUrl() throws DAOException {
		 List<Configuration> configurations = em.createNamedQuery("Configuration.findAll", Configuration.class).getResultList();
		 if(configurations.isEmpty()) {
			 throw new DAOException("90");
		 }
		 
		 return configurations.get(0).getWimsUrl();
	}
}
