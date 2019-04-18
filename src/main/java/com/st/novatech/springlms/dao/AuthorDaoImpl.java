package com.st.novatech.springlms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.st.novatech.springlms.model.Author;

/**
 * A DAO to provide an interface between the "author" table in the database and
 * the other layers of the code.
 *
 * @author Jonathan Lovelace
 */
public final class AuthorDaoImpl implements AuthorDao {
	/**
	 * The SQL query to update existing author rows.
	 */
	private final PreparedStatement updateStatement;
	/**
	 * The SQL query to delete author rows.
	 */
	private final PreparedStatement deleteStatement;
	/**
	 * The SQL query to find an author by ID.
	 */
	private final PreparedStatement findStatement;
	/**
	 * The SQL query to get all authors from the database.
	 */
	private final PreparedStatement getAllStatement;
	/**
	 * The SQL query to insert a new author into the database.
	 */
	private final PreparedStatement createStatement;
	/**
	 * The SQL query to get the newly created author's ID from the database.
	 */
	private final PreparedStatement findCreatedStatement;

	/**
	 * To create an instance of this DAO, the caller must supply the database
	 * connection.
	 *
	 * @param dbConnection the database connection.
	 * @throws SQLException on any unexpected database condition while setting up
	 *                      prepared statements
	 */
	public AuthorDaoImpl(final Connection dbConnection) throws SQLException {
		updateStatement = dbConnection.prepareStatement(
				"UPDATE `tbl_author` SET `authorName` = ? WHERE `authorId` = ?");
		deleteStatement = dbConnection
				.prepareStatement("DELETE FROM `tbl_author` WHERE `authorId` = ?");
		findStatement = dbConnection
				.prepareStatement("SELECT * FROM `tbl_author` WHERE `authorId` = ?");
		getAllStatement = dbConnection.prepareStatement("SELECT * FROM `tbl_author`");
		createStatement = dbConnection.prepareStatement(
				"INSERT INTO `tbl_author` (`authorName`) VALUES (?)");
		findCreatedStatement = dbConnection.prepareStatement(
				"SELECT `authorId` FROM `tbl_author` WHERE `authorName` = ? ORDER BY `authorId` DESC LIMIT 1");
	}

	@Override
	public void update(final Author author) throws SQLException {
		synchronized (updateStatement) {
			updateStatement.setString(1, author.getName());
			updateStatement.setInt(2, author.getId());
			updateStatement.executeUpdate();
		}
	}

	@Override
	public void delete(final Author author) throws SQLException {
		if (author != null) {
			synchronized (deleteStatement) {
				deleteStatement.setInt(1, author.getId());
				deleteStatement.executeUpdate();
			}
		}
	}

	@Override
	public Author get(final int id) throws SQLException {
		synchronized (findStatement) {
			findStatement.setInt(1, id);
			try (ResultSet result = findStatement.executeQuery()) {
				Author retval = null;
				while (result.next()) {
					if (retval == null) {
						retval = new Author(id, result.getString("authorName"));
					} else {
						throw new IllegalStateException("Multiple results for key");
					}
				}
				return retval;
			}
		}
	}

	@Override
	public List<Author> getAll() throws SQLException {
		final List<Author> retval = new ArrayList<>();
		synchronized (getAllStatement) {
			try (ResultSet result = getAllStatement.executeQuery()) {
				while (result.next()) {
					retval.add(new Author(result.getInt("authorId"),
							result.getString("authorName")));
				}
			}
		}
		return retval;
	}

	@Override
	public Author create(final String authorName) throws SQLException {
		synchronized (createStatement) {
			createStatement.setString(1, authorName);
			createStatement.executeUpdate();
			findCreatedStatement.setString(1, authorName);
			try (ResultSet result = findCreatedStatement.executeQuery()) {
				result.next();
				return new Author(result.getInt("authorId"), authorName);
			}
		}
	}
}
