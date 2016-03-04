package dao;

import java.io.Serializable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;



public abstract class AbstractGenericDao<T, K extends Serializable> implements IGenericDao<T, K> {

	@PersistenceContext(unitName = "bdd_xwims_PU")
    protected EntityManager em;

	private Class<T> type;

	
	public AbstractGenericDao(Class<T> clazz) {
		type = clazz;
	}
	
	@Override
	public T create(final T t) {
		em.persist(t);
		return t;
	}

	
	@Override
	public void delete(final T t) {
		em.remove(t);
	}

	@Override
	public T find(final K id) {
		return (T) em.find(type, id);
	}

	@Override
	public T update(final T t) {
		return em.merge(t);    
	}

	
}
