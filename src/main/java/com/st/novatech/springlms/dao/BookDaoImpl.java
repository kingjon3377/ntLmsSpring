package com.st.novatech.springlms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.st.novatech.springlms.model.Author;
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Publisher;

/**
 * A DAO to provide an interface between the "book" table in the database and
 * the other layers of the code.
 *
 * @author Jonathan Lovelace
 */
public final class BookDaoImpl implements BookDao {
	/**
	 * The SQL query to update existing book rows.
	 */
	private final PreparedStatement updateStatement;
	/**
	 * The SQL query to delete book rows.
	 */
	private final PreparedStatement deleteStatement;
	/**
	 * The SQL query to find a book by ID.
	 */
	private final PreparedStatement findStatement;
	/**
	 * The SQL query to get all books from the database.
	 */
	private final PreparedStatement getAllStatement;
	/**
	 * The SQL query to insert a new book into the database.
	 */
	private final PreparedStatement createBookStatement;
	/**
	 * The SQL query to get the newly created book's ID from the database.
	 */
	private final PreparedStatement findCreatedStatement;

	/**
	 * To construct an instance of a DAO, the caller must provide a connection to
	 * the database.
	 *
	 * @param dbConnection the database connection
	 * @throws SQLException on any unexpected database condition while setting up
	 *                      prepared statements
	 */
	public BookDaoImpl(final Connection dbConnection) throws SQLException {
		updateStatement = dbConnection.prepareStatement(
				"UPDATE `tbl_book` SET `title` = ?, `authId` = ?, `pubId` = ? WHERE `bookId` = ?");
		deleteStatement = dbConnection
				.prepareStatement("DELETE FROM `tbl_book` WHERE `bookID` = ?");
		findStatement = dbConnection.prepareStatement(
				"SELECT * FROM `tbl_book` LEFT JOIN `tbl_author` ON `tbl_book`.`authId` = `tbl_author`.`authorId` LEFT JOIN `tbl_publisher` ON `tbl_book`.`pubId` = `tbl_publisher`.`publisherId` WHERE `tbl_book`.`bookId` = ?");
		getAllStatement = dbConnection.prepareStatement(
				"SELECT * FROM `tbl_book` LEFT JOIN `tbl_author` ON `tbl_book`.`authId` = `tbl_author`.`authorId` LEFT JOIN `tbl_publisher` ON `tbl_book`.`pubId` = `tbl_publisher`.`publisherId`");
		createBookStatement = dbConnection.prepareStatement(
				"INSERT INTO `tbl_book` (`title`, `authId`, `pubId`) VALUES(?, ?, ?)");
		findCreatedStatement = dbConnection.prepareStatement(
				"SELECT `bookId` FROM `tbl_book` WHERE `title` = ? ORDER BY `bookId` DESC LIMIT 1");
	}

	@Override
	public void update(final Book book) throws SQLException {
		final Author author = book.getAuthor();
		final Publisher publisher = book.getPublisher();
		synchronized (updateStatement) {
			updateStatement.setString(1, book.getTitle());
			if (author == null) {
				updateStatement.setNull(2, Types.INTEGER);
			} else {
				updateStatement.setInt(2, author.getId());
			}
			if (publisher == null) {
				updateStatement.setNull(3, Types.INTEGER);
			} else {
				updateStatement.setInt(3, publisher.getId());
			}
			updateStatement.setInt(4, book.getId());
			updateStatement.executeUpdate();
		}
	}

	@Override
	public void delete(final Book book) throws SQLException {
		if (book != null) {
			synchronized (deleteStatement) {
				deleteStatement.setInt(1, book.getId());
				deleteStatement.executeUpdate();
			}
		}
	}

	@Override
	public Book get(final int id) throws SQLException {
		synchronized (findStatement) {
			findStatement.setInt(1, id);
			try (ResultSet result = findStatement.executeQuery()) {
				Book retval = null;
				while (result.next()) {
					if (retval == null) {
						Author author;
						final int authorId = result.getInt("authorId");
						if (result.wasNull()) {
							author = null;
						} else {
							author = new Author(authorId, result.getString("authorName"));
						}
						Publisher publisher;
						final int publisherId = result.getInt("publisherId");
						if (result.wasNull()) {
							publisher = null;
						} else {
							publisher = new Publisher(publisherId,
									result.getString("publisherName"),
									Optional.ofNullable(
											result.getString("publisherAddress"))
											.orElse(""),
									Optional.ofNullable(
											result.getString("publisherPhone"))
											.orElse(""));
						}
						retval = new Book(id, result.getString("title"), author,
								publisher);
					} else {
						throw new IllegalStateException("Multiple results for key");
					}
				}
				return retval;
			}
		}
	}

	@Override
	public List<Book> getAll() throws SQLException {
		final List<Book> retval = new ArrayList<>();
		synchronized (getAllStatement) {
			try (ResultSet result = getAllStatement.executeQuery()) {
				while (result.next()) {
					Author author;
					final int authorId = result.getInt("authorId");
					if (result.wasNull()) {
						author = null;
					} else {
						author = new Author(authorId, result.getString("authorName"));
					}
					Publisher publisher;
					final int publisherId = result.getInt("publisherId");
					if (result.wasNull()) {
						publisher = null;
					} else {
						publisher = new Publisher(publisherId,
								result.getString("publisherName"),
								Optional.ofNullable(
										result.getString("publisherAddress"))
										.orElse(""),
								Optional.ofNullable(
										result.getString("publisherPhone"))
										.orElse(""));
					}
					retval.add(new Book(result.getInt("bookId"),
							result.getString("title"), author, publisher));
				}
			}
			return retval;
		}
	}

	@Override
	public Book create(final String title, final Author author, final Publisher publisher)
			throws SQLException {
		synchronized (createBookStatement) {
			createBookStatement.setString(1, title);
			findCreatedStatement.setString(1, title);
			if (author == null) {
				createBookStatement.setNull(2, Types.INTEGER);
			} else {
				createBookStatement.setInt(2, author.getId());
			}
			if (publisher == null) {
				createBookStatement.setNull(3, Types.INTEGER);
			} else {
				createBookStatement.setInt(3, publisher.getId());
			}
			createBookStatement.executeUpdate();
			try (ResultSet result = findCreatedStatement.executeQuery()) {
				result.next();
				return new Book(result.getInt("bookId"), title, author, publisher);
			}
		}
	}
}
