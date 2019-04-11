package com.st.novatech.springlms.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.st.novatech.springlms.model.Author;
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Borrower;
import com.st.novatech.springlms.model.Branch;
import com.st.novatech.springlms.model.Loan;
import com.st.novatech.springlms.model.Publisher;

/**
 * A DAO to provide an interface between the "loans" table in the database and
 * the other layers of the code.
 *
 * @author Jonathan Lovelace
 */
public final class BookLoansDaoImpl implements BookLoansDao {
	/**
	 * The SQL query to update existing loan rows.
	 */
	private final PreparedStatement updateStatement;
	/**
	 * The SQL query to delete loan rows.
	 */
	private final PreparedStatement deleteStatement;
	/**
	 * The SQL query to find a loan by its book, branch, and borrower.
	 */
	private final PreparedStatement findStatement;
	/**
	 * The SQL query to get all loans from the database.
	 */
	private final PreparedStatement getAllStatement;
	/**
	 * The SQL query to insert a new loan into the database.
	 */
	private final PreparedStatement createStatement;

	/**
	 * To construct an instance of a DAO, the caller must provide a connection to
	 * the database.
	 *
	 * @param dbConnection the database connection
	 * @throws SQLException on any unexpected database condition while setting up
	 *                      prepared statements
	 */
	public BookLoansDaoImpl(final Connection dbConnection) throws SQLException {
		createStatement = dbConnection.prepareStatement(
				"INSERT INTO `tbl_book_loans` (`bookId`, `branchId`, `cardNo`, `dateOut`, `dueDate`) VALUES (?, ?, ?, ?, ?)");
		updateStatement = dbConnection.prepareStatement(
				"UPDATE `tbl_book_loans` SET `dateOut` = ?, `dueDate` = ? WHERE `bookId` = ? AND `branchId` = ? AND `cardNo` = ?");
		deleteStatement = dbConnection.prepareStatement(
				"DELETE FROM `tbl_book_loans` WHERE `bookId` = ? AND `branchId` = ? AND `cardNo` = ?");
		findStatement = dbConnection.prepareStatement(
				"SELECT * FROM `tbl_book_loans` WhERE `bookId` = ? AND `branchId` = ? AND `cardNo` = ?");
		getAllStatement = dbConnection.prepareStatement(
				"SELECT * FROM `tbl_book_loans` INNER JOIN `tbl_book` ON `tbl_book`.`bookId` = `tbl_book_loans`.`bookId` LEFT JOIN `tbl_author` ON `tbl_book`.`authId` = `tbl_author`.`authorId` LEFT JOIN `tbl_publisher` ON `tbl_book`.`pubId` = `tbl_publisher`.`publisherId` INNER JOIN `tbl_library_branch` ON `tbl_book_loans`.`branchId` = `tbl_library_branch`.`branchId` INNER JOIN `tbl_borrower` ON `tbl_borrower`.`cardNo` = `tbl_book_loans`.`cardNo`");
	}

	@Override
	public Loan create(final Book book, final Borrower borrower, final Branch branch,
			final LocalDateTime dateOut, final LocalDate dueDate)
			throws SQLException {
		synchronized (createStatement) {
			createStatement.setInt(1, book.getId());
			createStatement.setInt(2, branch.getId());
			createStatement.setInt(3, borrower.getCardNo());
			if (dateOut == null) {
				createStatement.setNull(4, Types.DATE);
			} else {
				createStatement.setDate(4, Date.valueOf(dateOut.toLocalDate()));
			}
			if (dueDate == null) {
				createStatement.setNull(5, Types.DATE);
			} else {
				createStatement.setDate(5, Date.valueOf(dueDate));
			}
			createStatement.executeUpdate();
		}
		return new Loan(book, borrower, branch, dateOut, dueDate);
	}

	@Override
	public void update(final Loan loan) throws SQLException {
		if (loan != null) {
			synchronized (updateStatement) {
				final LocalDateTime dateOut = loan.getDateOut();
				if (dateOut == null) {
					updateStatement.setNull(1, Types.DATE);
				} else {
					updateStatement.setDate(1, Date.valueOf(dateOut.toLocalDate()));
				}
				final LocalDate dueDate = loan.getDueDate();
				if (dueDate == null) {
					updateStatement.setNull(2, Types.DATE);
				} else {
					updateStatement.setDate(2, Date.valueOf(dueDate));
				}
				updateStatement.setInt(3, loan.getBook().getId());
				updateStatement.setInt(4, loan.getBranch().getId());
				updateStatement.setInt(5, loan.getBorrower().getCardNo());
				updateStatement.executeUpdate();
			}
		}
	}

	@Override
	public void delete(final Loan loan) throws SQLException {
		if (loan != null) {
			synchronized (deleteStatement) {
				deleteStatement.setInt(1, loan.getBook().getId());
				deleteStatement.setInt(2, loan.getBranch().getId());
				deleteStatement.setInt(3, loan.getBorrower().getCardNo());
				deleteStatement.executeUpdate();
			}
		}
	}

	@Override
	public Loan get(final Book book, final Borrower borrower, final Branch branch)
			throws SQLException {
		if (book == null || borrower == null || branch == null) {
			return null;
		}
		synchronized (findStatement) {
			findStatement.setInt(1, book.getId());
			findStatement.setInt(2, branch.getId());
			findStatement.setInt(3, borrower.getCardNo());
			try (ResultSet result = findStatement.executeQuery()) {
				Loan retval = null;
				while (result.next()) {
					if (retval == null) {
						retval = new Loan(book, borrower, branch,
								Optional.ofNullable(result.getDate("dateOut"))
										.map(Date::toLocalDate)
										.map(LocalDate::atStartOfDay).orElse(null),
								Optional.ofNullable(result.getDate("dueDate"))
										.map(Date::toLocalDate).orElse(null));
					} else {
						throw new IllegalStateException("Multiple results for key");
					}
				}
				return retval;
			}
		}
	}

	@Override
	public List<Loan> getAll() throws SQLException {
		final List<Loan> retval = new ArrayList<>();
		synchronized (getAllStatement) {
			try (ResultSet result = getAllStatement.executeQuery()) {
				while (result.next()) {
					final int authorId = result.getInt("authorId");
					final Author author;
					if (result.wasNull()) {
						author = null;
					} else {
						author = new Author(authorId, result.getString("authorName"));
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
					final Borrower borrower = new Borrower(result.getInt("cardNo"),
							Optional.ofNullable(result.getString("name")).orElse(""),
							Optional.ofNullable(result.getString("address"))
									.orElse(""),
							Optional.ofNullable(result.getString("phone"))
									.orElse(""));
					final Branch branch = new Branch(result.getInt("branchId"),
							Optional.ofNullable(result.getString("branchName"))
									.orElse(""),
							Optional.ofNullable(result.getString("branchAddress"))
									.orElse(""));
					retval.add(new Loan(book, borrower, branch,
							Optional.ofNullable(result.getDate("dateOut"))
									.map(Date::toLocalDate)
									.map(LocalDate::atStartOfDay).orElse(null),
							Optional.ofNullable(result.getDate("dueDate"))
									.map(Date::toLocalDate).orElse(null)));
				}
			}
			return retval;
		}
	}
}
