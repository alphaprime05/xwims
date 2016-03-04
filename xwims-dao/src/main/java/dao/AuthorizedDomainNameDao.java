package dao;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import entity.AuthorizedDomainName;
import exception.DAOException;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class AuthorizedDomainNameDao extends AbstractGenericDao<AuthorizedDomainName, String>{

	@Resource
	private UserTransaction utx;
	
	public AuthorizedDomainNameDao() {
		super(AuthorizedDomainName.class);
	}

	/**
	 * Add a new domain in database
	 * @param domainName The domain name
	 * @throws DAOException if a problem occurred during the creation
	 */
	public void addAuthorizedDomain(String domainName) throws DAOException {
		try{
			utx.begin();
			AuthorizedDomainName authorizedDomainName = new AuthorizedDomainName();
			authorizedDomainName.setDomain(domainName);
			create(authorizedDomainName);
			utx.commit();
		}catch(NotSupportedException | SecurityException | IllegalStateException | RollbackException | HeuristicMixedException
				| HeuristicRollbackException | SystemException e){
			try {
				utx.rollback();
			} catch (IllegalStateException | SecurityException | SystemException e1) {
				throw new DAOException("9");
			}
			throw new DAOException("9");
		}

	}


}
