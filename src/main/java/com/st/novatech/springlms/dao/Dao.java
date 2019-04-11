package com.st.novatech.springlms.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * The base interface for almost all Data-Access Objects.
 *
 * @author Salem Ozaki
 * @author Jonathan Lovelace
 *
 * @param <T> the type of model object this DAO deals primarily with
 */
public interface Dao<T> {
	/**
	 * Update the database representation of the given object to match its
	 * properties.
	 *
	 * @param t the object to update the database to match
	 * @throws SQLException on unexpected error dealing with the database
	 */
	void update(T t) throws SQLException;
	/**
	 * Remove the representation of the given object from the database.
	 * @param t the object to remove from the database.
	 * @throws SQLException on unexpected error dealing with the database
	 */
	void delete(T t) throws SQLException;
	/**
	 * Retrieve an object from the database table by its ID.
	 * @param id the ID number of the object to retrieve.
	 * @return the object represented by that ID number
	 * @throws SQLException on unexpected error dealing with the database
	 */
	T get(int id) throws SQLException;

	/**
	 * Get the list of all entries in this table. (The order in the list should not
	 * be relied upon.)
	 *
	 * @return the collection of all entries in this table
	 * @throws SQLException on unexpected error dealing with the database
	 */
	List<T> getAll() throws SQLException;
}
