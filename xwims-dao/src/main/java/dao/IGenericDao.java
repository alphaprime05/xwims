package dao;

import java.io.Serializable;

public interface IGenericDao<T, K extends Serializable> {

	/**
	 * Create a new object T in the database
	 * @param t
	 * @return the object created
	 */
	public abstract T create(T t);

	/**
	 * Delete the object T in the database
	 * @param t 
	 */
	public void delete(final T t);

	/**
	 * Find a element by is id in the database
	 * @param id
	 * @return the object correspond to the id
	 */
	public abstract T find(K id);

	/**
	 * Update the object T send in parameter in the database
	 * @param t 
	 * @return the upadated object
	 */
	public abstract T update(T t);

}