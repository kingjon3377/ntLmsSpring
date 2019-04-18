package com.st.novatech.springlms.dao;

import java.sql.SQLException;

import com.st.novatech.springlms.model.Author;
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Publisher;

/**
 * A Data Access Object interface to access the table of books.
 *
 * @author Salem Ozaki
 * @author Jonathan Lovelace
 */
public interface BookDao extends Dao<Book> {
	/**
	 * Create a book and add it to the database.
	 *
	 * @param author    the author of the book.
	 * @param publisher the publisher of the book.
	 * @return the created book
	 * @throws SQLException on unexpected error dealing with the database
	 */
	Book create(String title, Author author, Publisher publisher) throws SQLException;
}
