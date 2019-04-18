package com.st.novatech.springlms.dao;

import java.sql.SQLException;

import com.st.novatech.springlms.model.Author;

/**
 * A Data Access Object interface to access the table of authors.
 *
 * @author Salem Ozaki
 * @author Jonathan Lovelace
 */
public interface AuthorDao extends Dao<Author> {
	/**
	 * Create an author object and add it to the database.
	 *
	 * @param authorName the name of the author
	 * @return the created author
	 * @throws SQLException on unexpected error dealing with the database
	 */
	Author create(String authorName) throws SQLException;
}
