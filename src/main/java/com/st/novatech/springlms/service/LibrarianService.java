package com.st.novatech.springlms.service;

import java.util.List;

import com.st.novatech.springlms.exception.TransactionException;
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Branch;
import com.st.novatech.springlms.model.BranchCopies;

/**
 * A service interface to ease the creation of a UI for librarians.
 *
 * @author Salem Ozaki
 * @author Jonathan Lovelace
 */
public interface LibrarianService extends Service {
	/**
	 * Update the database's record of the given branch to match the object's state.
	 * @param branch the branch to update in the database.
	 */
	void updateBranch(Branch branch) throws TransactionException;

	/**
	 * Set the number of copies of the given book that the given branch owns.
	 *
	 * @param branch     the branch in question
	 * @param book       the book in question
	 * @param noOfCopies the number of copies of that book at that branch
	 */
	void setBranchCopies(Branch branch, Book book, int noOfCopies) throws TransactionException;
	/**
	 * Get all books in the database.
	 * @return all books in the database
	 */
	List<Book> getAllBooks() throws TransactionException;

	/**
	 * Get all counts of copies that branches have.
	 *
	 * @return the collection of all copy counts in the database
	 */
	List<BranchCopies> getAllCopies() throws TransactionException;

	/**
	 * Get a branch in the database.
	 *
	 * @return a branch in the database
	 */
	Branch getbranch(int branchId) throws TransactionException;

	/**
	 * Get a book in the database.
	 *
	 * @return a book in the database
	 */
	Book getBook(int bookId) throws TransactionException;
	/**
	 * Get the number of copies of a given book on a given branch in the database.
	 *
	 * @return the number of copies
	 */
	int getCopies(Book book,Branch branch) throws TransactionException;
}
