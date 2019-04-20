package com.st.novatech.springlms.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.st.novatech.springlms.exception.RetrieveException;
import com.st.novatech.springlms.exception.TransactionException;
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Branch;
import com.st.novatech.springlms.model.BranchCopies;
import com.st.novatech.springlms.service.LibrarianService;

/**
 * Controller for Librarian Services.
 *
 * @author Al-amine AHMED MOUSSA
 */
@RestController
public final class LibrarianController {
	/**
	 * Service class used to handle requests.
	 */
	@Autowired
	private LibrarianService service;

	/**
	 * Get all library branches.
	 * @return the list of all library branches
	 * @throws TransactionException on error caught by the service layer.
	 */
	@GetMapping({ "/branches", "/branches/" })
	public List<Branch> getBranches() throws TransactionException {
		return service.getAllBranches();
	}

	/**
	 * Get all books in the database.
	 * @return the list of all books
	 * @throws TransactionException on error caught by the service layer
	 */
	@GetMapping({ "/books", "/books/" })
	public List<Book> getBooks() throws TransactionException {
		return service.getAllBooks();
	}

	/**
	 * Get a branch by ID number.
	 *
	 * @param branchId an ID number
	 * @return the branch with that ID
	 * @throws TransactionException on error caught by the service layer, or if no
	 *                              matching branch
	 */
	@GetMapping({ "/branch/{branchId}", "/branch/{branchId}/" })
	public Branch getBranch(@PathVariable("branchId") final int branchId)
			throws TransactionException {
		final Branch branch = service.getbranch(branchId);
		if (branch == null) {
			throw new RetrieveException("Branch not found");
		} else {
			return branch;
		}
	}

	/**
	 * Get a book by ID number.
	 *
	 * @param bookId an ID number
	 * @return the book with that ID
	 * @throws TransactionException on error caught by the service layer, or if no
	 *                              matching book
	 */
	@GetMapping({ "/book/{bookId}", "/book/{bookId}/" })
	public Book getBook(@PathVariable("bookId") final int bookId)
			throws TransactionException {
		final Book book = service.getBook(bookId);
		if (book == null) {
			throw new RetrieveException("Book not found");
		} else {
			return book;
		}
	}

	/**
	 * Update a branch record.
	 *
	 * @param branchId the ID number of the branch to update
	 * @param input    user-supplied data to set in the branch record
	 * @return the updated branch record
	 * @throws TransactionException on error caught by the service layer, or if no
	 *                              matching branch
	 */
	@PutMapping({ "/branch/{branchId}", "/branch/{branchId}/" })
	public Branch updateBranch(@PathVariable("branchId") final int branchId,
			@RequestBody final Branch input) throws TransactionException {
		final Branch branch = service.getbranch(branchId);
		if (branch == null) {
			throw new RetrieveException("Branch not found");
		} else {
			branch.setName(input.getName());
			branch.setAddress(input.getAddress());
			service.updateBranch(branch);
			return service.getbranch(branchId);
		}
	}

	/**
	 * Update the number of copies of a book held by a branch.
	 *
	 * @param branchId the ID of the branch
	 * @param bookId   the ID of the book
	 * @param copies   the new number of copies
	 * @return the updated copies record
	 * @throws TransactionException on error caught by the service layer, or if
	 *                              there is no such book or branch
	 */
	@PutMapping({ "/branch/{branchId}/book/{bookId}",
			"/branch/{branchId}/book/{bookId}/" })
	public BranchCopies updateBranchCopies(@PathVariable("branchId") final int branchId,
			@PathVariable("bookId") final int bookId,
			@RequestParam("noOfCopies") final int copies)
			throws TransactionException {
		service.setBranchCopies(service.getbranch(branchId), service.getBook(bookId),
				copies);
		final int foundNumberOfCopies = service.getCopies(service.getBook(bookId),
				service.getbranch(branchId));
		return new BranchCopies(service.getBook(bookId), service.getbranch(branchId),
				foundNumberOfCopies);
	}

	/**
	 * Get the number of copies held by a branch.
	 *
	 * @param branchId the ID number of a branch
	 * @param bookId   the ID number of a book
	 * @return the record of the number of copies of that book held by that branch
	 * @throws TransactionException on error caught by the service layer, or if
	 *                              there is no such book or branch
	 */
	@GetMapping({ "/branch/{branchId}/book/{bookId}",
			"/branch/{branchId}/book/{bookId}" })
	public BranchCopies getBranchCopies(@PathVariable("branchId") final int branchId,
			@PathVariable("bookId") final int bookId) throws TransactionException {
		return new BranchCopies(service.getBook(bookId),
				service.getbranch(branchId), service.getCopies(
						service.getBook(bookId), service.getbranch(branchId)));
	}

	/**
	 * Get the list of copies records for all books at all branches.
	 * @return the list of all copies records
	 * @throws TransactionException on error caught by the service layer
	 */
	@GetMapping({ "/branches/books/copies", "/branches/books/copies/" })
	public List<BranchCopies> getAllCopies()
			throws TransactionException {
		return service.getAllCopies();
	}
}