package com.st.novatech.springlms.dao;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Borrower;
import com.st.novatech.springlms.model.Branch;
import com.st.novatech.springlms.model.Loan;

/**
 * A Data Access Object interface to access the table of outstanding loans.
 *
 * @author Salem Ozaki
 * @author Jonathan Lovelace
 */
public interface BookLoansDao {
	/**
	 * Check out a book from a particular branch for a particular borrower, with the
	 * given date out and due date.
	 *
	 * @param book     the book to check out
	 * @param borrower the borrower who is checking out the book
	 * @param branch   the library branch from which the book is being checked out
	 * @param dateOut  the date (and time) the book was checked out
	 * @param dueDate  the date the book is due back
	 * @return the created loan object
	 * @throws SQLException on unexpected error dealing with the database
	 */
	Loan create(Book book, Borrower borrower, Branch branch, LocalDateTime dateOut, LocalDate dueDate) throws SQLException;
	/**
	 * Update the dates associated with the given loan.
	 * @param loan The loan in question
	 * @throws SQLException on unexpected error dealing with the database
	 */
	void update(Loan loan) throws SQLException;
	/**
	 * Delete a loan from the database.
	 * @param loan The loan to delete
	 * @throws SQLException on unexpected error dealing with the database
	 */
	void delete(Loan loan) throws SQLException;

	/**
	 * Get the loan in which the given borrower checked out the given book from the
	 * given branch.
	 *
	 * @param book     the book in question
	 * @param borrower the borrower in question
	 * @param branch   the branch in question
	 * @return the Loan object giving the dates associated with this loan
	 * @throws SQLException on unexpected error dealing with the database
	 */
	Loan get(Book book, Borrower borrower, Branch branch) throws SQLException;

	/**
	 * Get all outstanding loans from the database. Callers should not rely on the
	 * order.
	 *
	 * @return the list of all outstanding loans
	 * @throws SQLException on unexpected error dealing with the database
	 */
	List<Loan> getAll() throws SQLException;
}
