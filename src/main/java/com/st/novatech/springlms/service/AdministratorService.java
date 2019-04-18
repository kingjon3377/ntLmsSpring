package com.st.novatech.springlms.service;

import java.time.LocalDate;
import java.util.List;

import com.st.novatech.springlms.exception.TransactionException;
import com.st.novatech.springlms.model.Author;
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Borrower;
import com.st.novatech.springlms.model.Branch;
import com.st.novatech.springlms.model.Loan;
import com.st.novatech.springlms.model.Publisher;

/**
 * A service interface to ease the creation of a UI for administrative users.
 *
 * @author Salem Ozaki
 * @author Jonathan Lovelace
 */
public interface AdministratorService extends Service {
	/**
	 * Create a book with the given title, author, and publisher and store it in the
	 * database.
	 *
	 * @param title     the title of the book
	 * @param author    the author of the book
	 * @param publisher the publisher of the book
	 * @return the newly-created book
	 */
	Book createBook(String title, Author author, Publisher publisher) throws TransactionException;
//	/**
//	 * Create a book with the given title, author, and publisher and store it in the
//	 * database.
//	 *
//	 * @param title     the title of the book
//	 * @param author    the author of the book
//	 * @param publisher the publisher of the book
//	 * @return the newly-created book
//	 */
//	Book createBook(String, String author, String publisher); // not sure if this is necessary
	/**
	 * Update the database row representing the given book to match its current
	 * state.
	 *
	 * @param book the book to update in the database.
	 */
	void updateBook(Book book) throws TransactionException;
	/**
	 * Remove the given book from the database.
	 * @param book the book to remove
	 */
	void deleteBook(Book book) throws TransactionException;
	/**
	 * Get a list (order should not be relied on) of all the books in the database.
	 * @return all the books in the database.
	 */
	List<Book> getAllBooks() throws TransactionException;

	/**
	 * Create an author object and add the author to the database.
	 * @param name the name of the author
	 * @return the newly created Author object
	 */
	Author createAuthor(String name) throws TransactionException;

	/**
	 * Update the database record for the given author object to match its current
	 * state.
	 *
	 * @param author the author to update in the database.
	 */
	void updateAuthor(Author author) throws TransactionException;
	/**
	 * Remove the given author from the database.
	 * @param author the author to remove
	 */
	void deleteAuthor(Author author) throws TransactionException;
	/**
	 * Get a list (order should not be relied on) of all the authors in the database.
	 * @return all the authors in the database.
	 */
	List<Author> getAllAuthors() throws TransactionException;

	/**
	 * Create a publisher object, with no address or phone number, and add the
	 * publisher to the database.
	 *
	 * @param name the publisher's name
	 * @return the newly created publisher
	 */
	Publisher createPublisher(String name) throws TransactionException;

	/**
	 * Create a publisher object with full state and add the publisher to the
	 * database.
	 *
	 * @param name    the publisher's name
	 * @param address the publisher's address
	 * @param phone   the publisher's phone number
	 * @return the newly created publisher
	 */
	Publisher createPublisher(String name, String address, String phone) throws TransactionException;

	/**
	 * Update the database record representing the given publisher to match its
	 * state.
	 *
	 * @param publisher the publisher to update in the database
	 */
	void updatePublisher(Publisher publisher) throws TransactionException;
	/**
	 * Remove the given publisher from the database.
	 * @param publisher the publisher to remove.
	 */
	void deletePublisher(Publisher publisher) throws TransactionException;

	/**
	 * Get a list (order should not be relied on) of all the publishers in the
	 * database.
	 *
	 * @return all the publishers in the database.
	 */
	List<Publisher> getAllPublishers() throws TransactionException;

	/**
	 * Create a library branch object and add it to the database.
	 * @param name the name of the branch
	 * @param address the address of the branch
	 * @return the newly created branch object
	 */
	Branch createBranch(String name, String address) throws TransactionException;
	/**
	 * Remove the given branch from the database.
	 * @param branch the branch to remove
	 */
	void deleteBranch(Branch branch) throws TransactionException;
	/**
	 * Update the database row representing the given branch with its state.
	 * @param branch the branch to update in the database
	 */
	void updateBranch(Branch branch) throws TransactionException;
	// getAllBranches() is specified in the Service interface

	/**
	 * Create a Borrower object with the given properties and add the borrower to
	 * the database.
	 *
	 * @param name    the borrower's name
	 * @param address the borrower's address
	 * @param phone   the borrower's phone number
	 * @return the newly created borrower object
	 */
	Borrower createBorrower(String name, String address, String phone) throws TransactionException;

	/**
	 * Update the database row representing the given borrower with the object's
	 * state.
	 *
	 * @param borrower the borrower to update in the database
	 */
	void updateBorrower(Borrower borrower) throws TransactionException;
	/**
	 * Remove the given borrower from the database.
	 * @param borrower the borrower to remove
	 */
	void deleteBorrower(Borrower borrower) throws TransactionException;

	/**
	 * Get a list (order should not be relied on) of all the borrowers in the
	 * database.
	 *
	 * @return all the borrowers in the database.
	 */
	List<Borrower> getAllBorrowers() throws TransactionException;

	/**
	 * Override the due date for the given borrower's loan of the given book from
	 * the given branch, returning true on success and false if that borrower does
	 * not have that book out from that branch.
	 *
	 * @param book the book in question
	 * @param borrower the borrower in question
	 * @param branch the branch in question
	 * @param dueDate the new due date for the loan
	 * @return true on success, false if no such loan currently exists
	 */
	boolean overrideDueDateForLoan(Book book, Borrower borrower, Branch branch, LocalDate dueDate) throws TransactionException;

	/**
	 * Get a list (order should not be relied on) of all the loans in the
	 * database.
	 *
	 * @return all the loans in the database.
	 */
	List<Loan> getAllLoans() throws TransactionException;

	/**
	 * Get the borrower with the specified card number.
	 *
	 * @param cardNo the borrower's card number
	 * @return the borrower with that card number, or null if none.
	 */
	Borrower getBorrower(int cardNo) throws TransactionException;

	/**
	 * Get the author with the specified id.
	 *
	 * @param authorId is the author's Id number
	 * @return the author with that Id number, or null if none.
	 */
	Author getAuthor(int authorId) throws TransactionException;

	/**
	 * Get the publisher with the specified id.
	 *
	 * @param publisherId is the publisher's Id number
	 * @return the publisher with that Id number, or null if none.
	 */
	Publisher getPublisher(int publisherId) throws TransactionException;

	/**
	 * Get a branch in the database.
	 * 
	 * @return a branch in the database
	 */
	Branch getBranch(int branchId) throws TransactionException;

	/**
	 * Get a book in the database.
	 * 
	 * @return a book in the database
	 */
	Book getBook(int bookId) throws TransactionException;

	/**
	 * Get a Loan of a specific Borrower and Book and Branch in the database.
	 * 
	 * @return a Loan in the database
	 */
	Loan getLoan(int cardNo, int branchId, int bookId) throws TransactionException;
}
