package com.st.novatech.springlms.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.st.novatech.springlms.model.Author;
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Publisher;

/**
 * Tests of book DAO.
 *
 * @author Al Amine Ahmed Moussa
 * @author Jonathan Lovelace (integration and polishing)
 */
public class AlBookDaoTest {
	/**
	 * The DAO under test.
	 */
	private BookDao bookDao;

	/**
	 * The connection to the database.
	 */
	private Connection conn;

	/**
	 * Author DAO used in tests.
	 */
	private AuthorDao authorDao;
	/**
	 * Publisher DAO used in tests.
	 */
	private PublisherDao publisherDao;

	/**
	 * Set up the DB connection and the DAO before each test.
	 *
	 * @throws SQLException on database errors
	 * @throws IOException  on I/O error reading the database schema from file
	 */
	@BeforeEach
	public void setUp() throws SQLException, IOException {
		conn = InMemoryDBFactory.getConnection("library");
		authorDao = new AuthorDaoImpl(conn);
		bookDao = new BookDaoImpl(conn);
		publisherDao = new PublisherDaoImpl(conn);
	}

	/**
	 * Tear down the database after each test.
	 *
	 * @throws SQLException on database error while closing the connection
	 */
	@AfterEach
	public void tearDown() throws SQLException {
		conn.close();
	}

	/**
	 * Test that creating a book works.
	 * @throws SQLException on database error
	 */
	@Test
	public void createTest() throws SQLException {
		final String str1 = "Title1";

		final Author a = authorDao.get(1);
		final Publisher p = publisherDao.get(1);

		final Book book = bookDao.create(str1, a, p);
		assertEquals(str1, book.getTitle(), "created book has expected title");
	}

	/**
	 * Test that getting a book works.
	 * @throws SQLException on database error
	 */
	@Test
	public void testGet() throws SQLException {
		final Author a = authorDao.get(1);
		final Publisher p = publisherDao.get(1);
		final Book b = bookDao.create("50 down", a, p);

		assertEquals(b.getTitle(), bookDao.get(b.getId()).getTitle(),
				"retrieved book has expected title");
	}
}
