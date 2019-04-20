package com.st.novatech.springlms.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.st.novatech.springlms.exception.RetrieveException;
import com.st.novatech.springlms.exception.TransactionException;
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Borrower;
import com.st.novatech.springlms.model.Branch;
import com.st.novatech.springlms.model.Loan;
import com.st.novatech.springlms.service.AdministratorService;

/**
 * Controller for administrators with the power to manage branch and borrower
 * details and override due dates.
 *
 * <p>FIXME: Limit access to most of these endpoints to authorized users
 *
 * <p>FIXME: Commit after making changes to the database.
 * @author Jonathan Lovelace
 */
@RestController
public final class ExecutiveController {
	/**
	 * Service class used to handle requests.
	 */
	@Autowired
	private AdministratorService service;
	/**
	 * Get all branches from the database. Spring turns the list into JSON (or XML?).
	 * @return the list of all branches in the database.
	 * @throws TransactionException on internal error.
	 */
	// TODO: Uncomment once controllers are split for service-discovery refactoring
//	@GetMapping({"/branches", "/branches/"})
	public List<Branch> getBranches() throws TransactionException {
		return service.getAllBranches();
	}
	/**
	 * Get all borrowers from the database. Spring turns the list into JSON (or XML?).
	 * @return the list of all borrowers in the database
	 * @throws TransactionException on internal error
	 */
	@GetMapping({"/borrowers", "/borrowers/"})
	public List<Borrower> getBorrowers() throws TransactionException {
		return service.getAllBorrowers();
	}
	/**
	 * Get a branch by its ID number.
	 * @param branchId the ID number of the branch
	 * @return the branch with that ID
	 * @throws TransactionException if branch not found, or on internal error
	 */
	// TODO: Uncomment once controllers are split for service-discovery refactoring
//	@GetMapping({"/branch/{branchId}", "/branch/{branchId}/"})
	public Branch getBranch(@PathVariable("branchId") final int branchId)
			throws TransactionException {
		final Branch branch = service.getBranch(branchId);
		if (branch == null) {
			throw new RetrieveException("Branch not found");
		} else {
			return branch;
		}
	}
	/**
	 * Get a borrower by his or her card number.
	 * @param cardNumber the borrower's card number
	 * @return the borrower with that card number
	 * @throws TransactionException if borrower not found, or on internal error
	 */
	@GetMapping({"/borrower/{cardNumber}", "/borrower/{cardNumber}/"})
	public Borrower getBorrower(@PathVariable("cardNumber") final int cardNumber)
			throws TransactionException {
		final Borrower borrower = service.getBorrower(cardNumber);
		if (borrower == null) {
			throw new RetrieveException("Borrower not found");
		} else {
			return borrower;
		}
	}
	/**
	 * Update a branch by its ID number.
	 * @param branchId the ID number of the branch to update
	 * @param input the branch data to update
	 * @return the updated branch
	 * @throws TransactionException if author not found or on internal error
	 */
	// TODO: Uncomment once controllers are split for service-discovery refactoring
//	@PutMapping({ "/branch/{branchId}", "/branch/{branchId}/" })
	public Branch updateBranch(@PathVariable("branchId") final int branchId,
			@RequestBody final Branch input) throws TransactionException {
		final Branch branch = service.getBranch(branchId);
		if (branch == null) {
			throw new RetrieveException("Branch not found");
		} else {
			branch.setName(input.getName());
			branch.setAddress(input.getAddress());
			service.updateBranch(branch);
			return service.getBranch(branchId);
		}
	}
	/**
	 * Update a borrower by his or her card number.
	 * @param cardNumber the card number of the borrower to update
	 * @param input the borrower details to update
	 * @return the updated borrower
	 * @throws TransactionException if borrower not found or on internal error
	 */
	@PutMapping({ "/borrower/{cardNumber}", "/borrower/{cardNumber}/" })
	public Borrower updateBorrower(@PathVariable("cardNumber") final int cardNumber,
			@RequestBody final Borrower input) throws TransactionException {
		final Borrower borrower = service.getBorrower(cardNumber);
		if (borrower == null) {
			throw new RetrieveException("Borrower not found");
		} else {
			borrower.setName(input.getName());
			borrower.setAddress(input.getAddress());
			borrower.setPhone(input.getPhone());
			service.updateBorrower(borrower);
			return service.getBorrower(cardNumber);
		}
	}
	/**
	 * Create a branch with the given name and address.
	 * @param name the name of the branch
	 * @param address the address of the branch
	 * @return the created branch
	 * @throws TransactionException on internal error
	 */
	@PostMapping({"/branch", "/branch/"})
	public Branch createBranch(@RequestParam("name") final String name,
			@RequestParam(name = "address", defaultValue = "") final String address)
			throws TransactionException {
		return service.createBranch(name, address);
	}
	/**
	 * Create a borrower record with the given name, address, and phone data.
	 * @param name the name of the borrower
	 * @param address the address of the borrower
	 * @param phone the phone number of the borrower
	 * @return the created borrower record
	 * @throws TransactionException on internal error
	 */
	@PostMapping({"/borrower", "/borrower/"})
	public Borrower createBorrower(@RequestParam("name") final String name,
			@RequestParam(name = "address", defaultValue = "") final String address,
			@RequestParam(name = "phone", defaultValue = "") final String phone)
			throws TransactionException {
		return service.createBorrower(name, address, phone);
	}
	/**
	 * Delete the branch with the given ID.
	 * @param branchId the ID of the branch to delete.
	 * @throws TransactionException on internal error
	 */
	@DeleteMapping({"/branch/{branchId}", "/branch/{branchId}/"})
	public void deleteBranch(@PathVariable("branchId") final int branchId) throws TransactionException {
		final Branch branch = service.getBranch(branchId);
		if (branch != null) {
			service.deleteBranch(branch);
		}
	}
	/**
	 * Delete the borrower with the given card number.
	 * @param cardNumber the card number of the borrower to delete from the database
	 * @throws TransactionException on internal error
	 */
	@DeleteMapping({ "/borrower/{cardNumber}", "/borrower/{cardNumber}/" })
	public void deleteBorrower(@PathVariable("cardNumber") final int cardNumber)
			throws TransactionException {
		final Borrower borrower = service.getBorrower(cardNumber);
		if (borrower != null) {
			service.deleteBorrower(borrower);
		}
	}
	/**
	 * Override the due date of a loan.
	 * @param borrowerId the card number of the borrower who checked out the book in question
	 * @param branchId the ID number of the branch from which the book was checked out
	 * @param bookId the ID number of the book in question
	 * @param dueDate the new due date
	 * @return the updated loan record
	 * @throws TransactionException if no such borrower, branch, book, or loan, or on internal error
	 */
	@PutMapping("/loan/book/{bookId}/branch/{branchId}/borrower/{borrowerId}/due")
	public Loan overrideDueDate(@PathVariable("bookId") final int bookId,
			@PathVariable("branchId") final int branchId,
			@PathVariable("borrowerId") final int borrowerId,
			@RequestParam final LocalDate dueDate) throws TransactionException {
		final Book book = service.getBook(bookId);
		final Branch branch = service.getBranch(branchId);
		final Borrower borrower = service.getBorrower(borrowerId);
		Loan loan;
		if (book == null || branch == null || borrower == null) {
			throw new RetrieveException("No such loan");
		} else {
			loan = service.getLoan(borrowerId, branchId, bookId);
			if (loan == null) {
				throw new RetrieveException("No such loan");
			}
		}
		service.overrideDueDateForLoan(book, borrower, branch, dueDate);
		return service.getLoan(borrowerId, branchId, bookId);
	}
	/**
	 * Get the date a book is due back to the branch from which it was borrowed.
	 * @param borrowerId the card number of the borrower who checked out the book in question
	 * @param branchId the ID number of the branch from which the book was checked out
	 * @param bookId the ID number of the book in question
	 * @return the updated loan record
	 * @throws TransactionException if no such borrower, branch, book, or loan, or on internal error
	 */
	@GetMapping("/loan/book/{bookId}/branch/{branchId}/borrower/{borrowerId}/due")
	public LocalDate getDueDate(@PathVariable("bookId") final int bookId,
			@PathVariable("branchId") final int branchId,
			@PathVariable("borrowerId") final int borrowerId) throws TransactionException {
		if (service.getBook(bookId) == null || service.getBranch(branchId) == null
				|| service.getBorrower(borrowerId) == null) {
			throw new RetrieveException("No such loan");
		} else {
			final Loan loan = service.getLoan(borrowerId, branchId, bookId);
			if (loan == null) {
				throw new RetrieveException("No such loan");
			} else {
				return loan.getDueDate();
			}
		}
	}
}
