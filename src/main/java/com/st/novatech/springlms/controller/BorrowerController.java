package com.st.novatech.springlms.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.st.novatech.springlms.exception.AlreadyBorrowedException;
import com.st.novatech.springlms.exception.NoCopiesException;
import com.st.novatech.springlms.exception.RetrieveException;
import com.st.novatech.springlms.exception.TransactionException;
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Borrower;
import com.st.novatech.springlms.model.Branch;
import com.st.novatech.springlms.model.Loan;
import com.st.novatech.springlms.service.BorrowerService;

@RestController
public class BorrowerController {
	
	@Autowired
	BorrowerService borrowerService;
	
	/**
	 * allows a borrower to borrow a book from a branch and lets the client know of the status
	 * 
	 * @param cardNo	id for borrower
	 * @param branchId	id for branch
	 * @param bookId	id for book
	 * @return	Loans if created correctly with an appropriate http code,
	 * 	else an appropriate http error code
	 * @throws TransactionException		if something goes wrong with any of the transactions
	 * @throws AlreadyBorrowedException	if the borrrower already borrowed the requested
	 * 	book from the requested branch
	 * @throws NoCopiesException		if there are no copies for the requested book in
	 * 	the requested branch
	 */
	@RequestMapping(path = "/borrower/{cardNo}/branch/{branchId}/book/{bookId}/borrow", method = RequestMethod.POST)
	public ResponseEntity<Loan> borrowBook(@PathVariable("cardNo") int cardNo,
			@PathVariable("branchId") int branchId,
			@PathVariable("bookId") int bookId) throws TransactionException, AlreadyBorrowedException, NoCopiesException {
		Borrower foundBorrower = borrowerService.getBorrower(cardNo);
		Book foundBook = borrowerService.getBook(bookId);
		Branch foundBranch = borrowerService.getbranch(branchId);
		try {
			Loan foundLoan = borrowerService.getLoan(cardNo, branchId, bookId);
			if(foundLoan != null) {
				throw new AlreadyBorrowedException("You have already borrowed the requsted book");
			} else {
				Loan newLoan = borrowerService.borrowBook(foundBorrower, foundBook, foundBranch,
						LocalDateTime.now(), LocalDate.now().plusWeeks(1));
				if(newLoan == null) {
					throw new NoCopiesException("There are no copies for the requsted book in this library branch");
				} else {
					return new ResponseEntity<Loan>(newLoan, HttpStatus.CREATED);
				}
			}
		} catch (TransactionException exception) {
			if(exception.getSuppressed().length > 0) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
				throw exception;
			}
		}
	}
	
	/**
	 * To retrieve a list of book copies of a particular branch, the client must supply a branch id, which the server
	 * will use to get the associated branch entity, which is used to fetch the list of book copies of the
	 * requested branch
	 * 
	 * @param branchId	used to get a list of book copies associated with the given branchId (branch)
	 * @return	a list of book copies associated with the given branch Id if the branch associated to
	 * the branch id exists or an internal server error (500) if the roll back fails
	 * @throws TransactionException	A retrieval exception will be thrown if the branch associated to
	 * the branch id given does not exist or if the search for the book copies list failed.
	 */
	@RequestMapping(path = "/branch/{branchId}/books/copies", method = RequestMethod.GET)
	public ResponseEntity<Map<Book, Integer>> getAllBranchCopies(@PathVariable("branchId") int branchId) throws TransactionException {
		try {
			Branch foundBranch = borrowerService.getbranch(branchId);
			if(foundBranch == null) {
				throw new RetrieveException("Requested branch not found");
			}
			Map<Book, Integer> listOfAllBranchCopies = borrowerService.getAllBranchCopies(foundBranch);
			return new ResponseEntity<Map<Book, Integer>>(listOfAllBranchCopies, HttpStatus.OK);
		} catch (TransactionException exception) {
			if(exception.getSuppressed().length > 0) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
				throw exception;
			}
		}
	}
	
	/**
	 * For client who would like to return a book
	 * 
	 * @param cardNo	id for a particular borrower
	 * @param branchId	id for a particular branch
	 * @param bookId	id for a particular book
	 * @return			Success message with 204(NO_CONTENT) code if the book was returned correctly,
	 * 404(NOT_FOUND) if the entry of the given cardNo, branchId, and bookId does not exist,
	 * 409(CONFLICT) if the book is overdue, or returns a 500(INTERNAL_SERVER_ERROR) if the roll
	 * back fails
	 * @throws TransactionException Throws an UnknownSQLException if something goes wrong with
	 * the book copies and throws a DeleteException if something goes wrong with deleting the
	 * entry
	 */
	@DeleteMapping(path = "/borrower/{cardNo}/branch/{branchId}/book/{bookId}/return")
	public ResponseEntity<String> returnBook(@PathVariable("cardNo") int cardNo,
			@PathVariable("branchId") int branchId, @PathVariable("bookId") int bookId) throws TransactionException {
		try {
			Borrower borrower = borrowerService.getBorrower(cardNo);
			Branch branch = borrowerService.getbranch(branchId);
			Book book = borrowerService.getBook(bookId);
			if(borrower == null) {
				throw new RetrieveException("Requested borrower not found");
			} else if(branch == null) {
				throw new RetrieveException("Requested branch not found");
			} else if(book == null) {
				throw new RetrieveException("Requested book not found");
			} else {
				// non of the given ids were incorrect
				Boolean success = borrowerService.returnBook(borrower, book, branch, LocalDate.now());
				if(success == null) {
					return new ResponseEntity<String>("You (" + borrower.getName() + ") do not have " +
							book.getTitle() + " checkout from " + branch.getName(), HttpStatus.NOT_FOUND);
				} else if(success.booleanValue()) {
					return new ResponseEntity<String>("Successfully returned " + book.getTitle(), HttpStatus.NO_CONTENT);
				} else {
					return new ResponseEntity<String>("This book is overdue", HttpStatus.CONFLICT);
				}
			}
		} catch (TransactionException exception) {
			if(exception.getSuppressed().length > 0) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
				throw exception;
			}
		}
	}
	
	/**
	 * Get all branches from which the borrower has an outstanding book loan.
	 * 
	 * @param cardNo	id for a particular borrower
	 * @return			200(OK) if the borrower exists in the database and if everything goes correctly
	 * or will return 500(an internal server error) the roll back fails
	 * @throws TransactionException retrieve exception if it cannot find the given borrower
	 */
	@GetMapping(path = "/borrower/{cardNo}/loansWithBranch")
	public ResponseEntity<List<Branch>> getAllBranchesWithLoan(@PathVariable("cardNo") int cardNo) throws TransactionException {
		try {
			Borrower foundBorrower = borrowerService.getBorrower(cardNo);
			if(foundBorrower == null) {
				throw new RetrieveException("Requested borrower not found");
			}
			List<Branch> listOfBranchesForBorrowerWithLoans = borrowerService.getAllBranchesWithLoan(foundBorrower);
			return new ResponseEntity<List<Branch>>(listOfBranchesForBorrowerWithLoans, HttpStatus.OK);
		} catch (TransactionException exception) {
			if(exception.getSuppressed().length > 0) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
				throw exception;
			}
		}
	}
	
	/**
	 * Get all book loans the borrower has borrowed from any library branch.
	 * 
	 * @param cardNo	id for a particular borrower
	 * @return			200(OK) if the borrower exists in the database and if everything goes correctly
	 * or will return 500(an internal server error) the roll back fails
	 * @throws TransactionException	retrieve exception if it cannot find the given borrower
	 */
	@GetMapping(path = "/borrower/{cardNo}/borrowerLoans")
	public ResponseEntity<List<Loan>> getAllBorrowedBooks(@PathVariable("cardNo") int cardNo) throws TransactionException {
		try {
			Borrower foundBorrower = borrowerService.getBorrower(cardNo);
			if(foundBorrower == null) {
				throw new RetrieveException("Requested borrower not found");
			}
			List<Loan> listOfLoansForBorrower = borrowerService.getAllBorrowedBooks(foundBorrower);
			return new ResponseEntity<List<Loan>>(listOfLoansForBorrower, HttpStatus.OK);
		} catch (TransactionException exception) {
			if(exception.getSuppressed().length > 0) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
				throw exception;
			}
		}
	}
	
	/**
	 * Give the client a borrower with a given cardNo
	 * 
	 * @param cardNo id for a particular borrower
	 * @return a ResponseEntity of a borrower with an ok code
	 * or will return 500(an internal server error) the roll back fails
	 * @throws TransactionException retrieve exception if it cannot find the requested borrower
	 */
	@RequestMapping(path="/borrower/{cardNo}", method = RequestMethod.GET)
	public ResponseEntity<Borrower> getBorrowerById(@PathVariable("cardNo") int cardNo) throws TransactionException {
		try {
			Borrower foundBorrower = borrowerService.getBorrower(cardNo);
			if(foundBorrower == null) {
				throw new RetrieveException("Requested borrower not found");
			} else {
				return new ResponseEntity<Borrower>(foundBorrower, HttpStatus.OK);
			}
		} catch (TransactionException exception) {
			if(exception.getSuppressed().length > 0) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
				throw exception;
			}
		}
	}
	
	/**
	 * Give the client a branch with a given branchId
	 * 
	 * @param branchId	id for a particular branch
	 * @return	a ResponseEntity of a branch with an 200(OK) code
	 * or will return 500(an internal server error) the roll back fails
	 * @throws TransactionException retrieve exception if it cannot find the requested branch
	 */
	@GetMapping(path = "/branch/{branchId}")
	public ResponseEntity<Branch> getbranch(@PathVariable("branchId") int branchId) throws TransactionException {
		try {
			Branch foundBranch = borrowerService.getbranch(branchId);
			if(foundBranch == null) {
				throw new RetrieveException("Requested branch not found");
			} else {
				return new ResponseEntity<Branch>(foundBranch, HttpStatus.OK);
			}
		} catch (TransactionException exception) {
			if(exception.getSuppressed().length > 0) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
				throw exception;
			}
		}
	}
	
	/**
	 * Give the client a book with a given bookId
	 * 
	 * @param bookId	id for a particular branch
	 * @return	a ResponseEntity of a book with an 200(OK) code
	 * or will return 500(an internal server error) the roll back fails
	 * @throws TransactionException	retrieve exception if it cannot find the requested book
	 */
	@GetMapping(path = "/book/{bookId}")
	public ResponseEntity<Book> getBook(@PathVariable("bookId") int bookId) throws TransactionException {
		try {
			Book foundBook = borrowerService.getBook(bookId);
			if(foundBook == null) {
				throw new RetrieveException("Requested book not found");
			} else {
				return new ResponseEntity<Book>(foundBook, HttpStatus.OK);
			}
		} catch (TransactionException exception) {
			if(exception.getSuppressed().length > 0) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
				throw exception;
			}
		}
	}
	
	/**
	 * gives client a loan object based on uri given by client
	 * 
	 * @param cardNo	id for a borrower
	 * @param branchId	id for a branch
	 * @param bookId	id for a book
	 * @return a ResponseEntity of a loan with an ok code or an appropriate http error code
	 * @throws TransactionException send an internal server error code if rollback fails,
	 * 	else sends a not found code
	 */
	@RequestMapping(path = "/borrower/{cardNo}/branch/{branchId}/book/{bookId}", method = RequestMethod.GET)
	public ResponseEntity<Loan> getLoanByIds(@PathVariable("cardNo") int cardNo,
			@PathVariable("branchId") int branchId,
			@PathVariable("bookId") int bookId) throws TransactionException {
		try {
			Loan loan = borrowerService.getLoan(cardNo, branchId, bookId);
			if(loan == null) {
				throw new RetrieveException("Requested loan not found");
			} else {
				return new ResponseEntity<Loan>(loan, HttpStatus.OK);
			}
		} catch (TransactionException exception) {
			if(exception.getSuppressed().length > 0) {
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
				throw exception;
			}
		}
	}
}
