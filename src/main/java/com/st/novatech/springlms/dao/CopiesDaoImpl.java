package com.st.novatech.springlms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import com.st.novatech.springlms.model.Author;
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Branch;
import com.st.novatech.springlms.model.Publisher;
/**
 * A DAO to provide an interface between the "copies" table in the database and
 * the other layers of the code.
 *
 * @author Jonathan Lovelace
 */
public final class CopiesDaoImpl implements CopiesDao {
	/**
	 * The name of the "number of copies" column in the database table, extracted
	 * because it was used four times in this class.
	 */
	private static final String N_COPIES_FIELD = "noOfCopies";
	/**
	 * The SQL query to find a copies row in the database by book and branch.
	 */
	private final PreparedStatement findStatement;
	/**
	 * The SQL query to insert a copies record into the database where there wasn't
	 * one before.
	 */
	private final PreparedStatement insertStatement;
	/**
	 * The SQL query to update a copies record in the database.
	 */
	private final PreparedStatement updateStatement;
	/**
	 * The SQL query to find all copies records in the database for a particular
	 * branch.
	 */
	private final PreparedStatement findByBranchStatement;
	/**
	 * The SQL query to find all copies records in the database involving a
	 * particular book.
	 */
	private final PreparedStatement findByBookStatement;
	/**
	 * The SQL query to get all copies records from the database.
	 */
	private final PreparedStatement getAllStatement;
	/**
	 * The SQL query to delete a copies record.
	 */
	private final PreparedStatement deleteStatement;

	/**
	 * To construct an instance of a DAO, the caller must provide a connection to
	 * the database.
	 *
	 * @param dbConnection the database connection
	 * @throws SQLException on any unexpected database condition while setting up
	 *                      prepared statements
	 */
	public CopiesDaoImpl(final Connection dbConnection) throws SQLException {
		getAllStatement = dbConnection.prepareStatement(
				"SELECT * FROM `tbl_book_copies` INNER JOIN `tbl_library_branch` ON `tbl_book_copies`.`branchId` = `tbl_library_branch`.`branchId` INNER JOIN `tbl_book` ON `tbl_book_copies`.`bookId` = `tbl_book`.`bookId` LEFT JOIN `tbl_publisher` ON `tbl_book`.`pubId` = `tbl_publisher`.`publisherId` LEFT JOIN `tbl_author` ON `tbl_author`.`authorId` = `tbl_book`.`authId`");
		findStatement = dbConnection.prepareStatement(
				"SELECT * FROM `tbl_book_copies` WHERE `bookId` = ? AND `branchId` = ?");
		insertStatement = dbConnection.prepareStatement(
				"INSERT INTO `tbl_book_copies` (`bookId`, `branchId`, `noOfCopies`) VALUES (?, ?, ?)");
		updateStatement = dbConnection.prepareStatement(
				"UPDATE `tbl_book_copies` SET `noOfCopies` = ? WHERE `bookId` = ? AND `branchId` = ?");
		findByBranchStatement = dbConnection.prepareStatement(
				"SELECT * FROM `tbl_book_copies` INNER JOIN `tbl_book` ON `tbl_book_copies`.`bookId` = `tbl_book`.`bookId` LEFT JOIN `tbl_publisher` ON `tbl_book`.`pubId` = `tbl_publisher`.`publisherId` LEFT JOIN `tbl_author` ON `tbl_author`.`authorId` = `tbl_book`.`authId` WHERE `branchId` = ?");
		findByBookStatement = dbConnection.prepareStatement(
				"SELECT * FROM `tbl_book_copies` INNER JOIN `tbl_library_branch` ON `tbl_book_copies`.`branchId` = `tbl_library_branch`.`branchId` WHERE `bookId` = ?");
		deleteStatement = dbConnection.prepareStatement(
				"DELETE FROM `tbl_book_copies` WHERE `bookId` = ? AND `branchId` = ?");
	}

	@Override
	public int getCopies(final Branch branch, final Book book) throws SQLException {
		if (branch == null || book == null) {
			return 0; // TODO: Throw IllegalArgumentException instead?
		}
		synchronized (findStatement) {
			findStatement.setInt(1, book.getId());
			findStatement.setInt(2, branch.getId());
			try (ResultSet result = findStatement.executeQuery()) {
				OptionalInt copies = OptionalInt.empty();
				while (result.next()) {
					if (copies.isPresent()) {
						throw new IllegalStateException("Multiple results for key");
					} else {
						copies = OptionalInt.of(result.getInt(N_COPIES_FIELD));
					}
				}
				return copies.orElse(0);
			}
		}
	}

	@Override
	public void setCopies(final Branch branch, final Book book, final int noOfCopies)
			throws SQLException {
		if (noOfCopies < 0) {
			throw new IllegalArgumentException(
					"Number of copies must be nonnegative");
		} else if (book == null || branch == null) {
			// TODO: throw IllegalArgumentException?
		} else if (noOfCopies == 0) {
			synchronized (deleteStatement) {
				deleteStatement.setInt(1, book.getId());
				deleteStatement.setInt(2, branch.getId());
				deleteStatement.executeUpdate();
			}
		} else if (getCopies(branch, book) == 0) {
			// TODO: Use INSERT ... ON DUPLICATE KEY UPDATE
			synchronized (insertStatement) {
				insertStatement.setInt(1, book.getId());
				insertStatement.setInt(2, branch.getId());
				insertStatement.setInt(3, noOfCopies);
				insertStatement.executeUpdate();
			}
		} else {
			synchronized (updateStatement) {
				updateStatement.setInt(1, book.getId());
				updateStatement.setInt(2, branch.getId());
				updateStatement.setInt(3, noOfCopies);
				updateStatement.executeUpdate();
			}
		}
	}

	@Override
	public Map<Book, Integer> getAllBranchCopies(final Branch branch)
			throws SQLException {
		if (branch == null) {
			return Collections.emptyMap();
		} else {
			final Map<Book, Integer> retval = new HashMap<>();
			synchronized (findByBranchStatement) {
				findByBranchStatement.setInt(1, branch.getId());
				try (ResultSet result = findByBranchStatement.executeQuery()) {
					while (result.next()) {
						final int authorId = result.getInt("authorId");
						final Author author;
						if (result.wasNull()) {
							author = null;
						} else {
							author = new Author(authorId,
									result.getString("authorName"));
						}
						final int publisherId = result.getInt("publisherId");
						final Publisher publisher;
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
						final Book book = new Book(result.getInt("bookId"),
								result.getString("title"), author, publisher);
						retval.put(book, result.getInt(N_COPIES_FIELD));
					}
				}
				return retval;
			}
		}
	}

	@Override
	public Map<Branch, Integer> getAllBookCopies(final Book book)
			throws SQLException {
		if (book == null) {
			return Collections.emptyMap();
		} else {
			final Map<Branch, Integer> retval = new HashMap<>();
			synchronized (findByBookStatement) {
				findByBookStatement.setInt(1, book.getId());
				try (ResultSet result = findByBookStatement.executeQuery()) {
					while (result.next()) {
						final Branch branch = new Branch(result.getInt("branchId"),
								Optional.ofNullable(result.getString("branchName"))
										.orElse(""),
								Optional.ofNullable(
										result.getString("branchAddress"))
										.orElse(""));
						retval.put(branch, result.getInt(N_COPIES_FIELD));
					}
				}
				return retval;
			}
		}
	}

	@Override
	public Map<Branch, Map<Book, Integer>> getAllCopies() throws SQLException {
		final Map<Branch, Map<Book, Integer>> retval = new HashMap<>();
		synchronized (getAllStatement) {
			try (ResultSet result = getAllStatement.executeQuery()) {
				while (result.next()) {
					final Branch branch = new Branch(result.getInt("branchId"),
							Optional.ofNullable(result.getString("branchName"))
									.orElse(""),
							Optional.ofNullable(result.getString("branchAddress"))
									.orElse(""));
					Map<Book, Integer> innerMap;
					if (retval.containsKey(branch)) {
						innerMap = retval.get(branch);
					} else {
						innerMap = new HashMap<>();
						retval.put(branch, innerMap);
					}
					final int authorId = result.getInt("authorId");
					final Author author;
					if (result.wasNull()) {
						author = null;
					} else {
						author = new Author(authorId,
								result.getString("authorName"));
					}
					final int publisherId = result.getInt("publisherId");
					final Publisher publisher;
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
					final Book book = new Book(result.getInt("bookId"),
							result.getString("title"), author, publisher);
					innerMap.put(book, result.getInt(N_COPIES_FIELD));
				}
			}
			return retval;
		}
	}
}
