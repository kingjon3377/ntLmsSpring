package com.st.novatech.springlms.dao;

import java.sql.SQLException;
import java.util.Map;

import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Branch;

/**
 * A Data Access Object interface to access the number of copies of books in
 * branches.
 *
 * @author Salem Ozaki
 * @author Jonathan Lovelace
 */
public interface CopiesDao {
	/**
	 * Get the number of copies of a book held by a particular branch.
	 *
	 * @param branch The branch in question.
	 * @param book   The book in question.
	 * @return the number of copies held by that branch; if none, 0.
	 * @throws SQLException on unexpected error in dealing with the database.
	 */
	int getCopies(Branch branch, Book book) throws SQLException;

	/**
	 * Set the number of copies of a book held by a particular branch. If the number
	 * is set to 0, the row is deleted from the database.
	 *
	 * @param branch     the branch in question
	 * @param book       the book in question
	 * @param noOfCopies the number of copies held by that branch; must not be
	 *                   negative.
	 * @throws SQLException on unexpected error in dealing with the database.
	 */
	void setCopies(Branch branch, Book book, int noOfCopies) throws SQLException;

	/**
	 * Retrieve all copies held by the given branch, as a mapping from books to the
	 * number held.
	 *
	 * @param branch the branch in question
	 * @return the number of copies of all books the branch holds.
	 * @throws SQLException on unexpected error in dealing with the database.
	 */
	Map<Book, Integer> getAllBranchCopies(Branch branch) throws SQLException;

	/**
	 * Retrieve all copies of the given book held by any branch, as a mapping from
	 * branches to the number of copies of the book they hold.
	 *
	 * @param book the book in question
	 * @return the number of copies of that book in each branch that holds it.
	 * @throws SQLException on unexpected error in dealing with the database.
	 */
	Map<Branch, Integer> getAllBookCopies(Book book) throws SQLException;

	/**
	 * Retrieve all copies of all books held by all branches, as a mapping from
	 * branches to mappings from books to number of copies.
	 *
	 * @return the number of copies of all books in all branches.
	 * @throws SQLException on unexpected error in dealing with the database.
	 */
	Map<Branch, Map<Book, Integer>> getAllCopies() throws SQLException;
}
