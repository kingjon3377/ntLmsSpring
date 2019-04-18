package com.st.novatech.springlms.service;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import com.st.novatech.springlms.dao.BookDao;
import com.st.novatech.springlms.dao.BookLoansDao;
import com.st.novatech.springlms.dao.BorrowerDao;
import com.st.novatech.springlms.dao.CopiesDao;
import com.st.novatech.springlms.dao.LibraryBranchDao;
import com.st.novatech.springlms.exception.DeleteException;
import com.st.novatech.springlms.exception.InsertException;
import com.st.novatech.springlms.exception.RetrieveException;
import com.st.novatech.springlms.exception.TransactionException;
import com.st.novatech.springlms.exception.UnknownSQLException;
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Borrower;
import com.st.novatech.springlms.model.Branch;
import com.st.novatech.springlms.model.BranchCopies;
import com.st.novatech.springlms.model.Loan;

/**
 * The "service" class to help UIs for borrowers.
 *
 * @author Jonathan Lovelace
 */
@Service("BorrowerService")
public final class BorrowerServiceImpl implements BorrowerService {
	/**
	 * The DAO for the "branches" table.
	 */
	@Autowired
	private LibraryBranchDao branchDao;
	/**
	 * The DAO for the "loans" table.
	 */
	@Autowired
	private BookLoansDao loanDao;
	/**
	 * The DAO for the "copies" table.
	 */
	@Autowired
	private CopiesDao copiesDao;
	/**
	 * The DAO for the "borrowers" table.
	 */
	@Autowired
	private BorrowerDao borrowerDao;
	/**
	 * The clock to get "the current time" from.
	 */
	private final Clock clock;
	/**
	 * Logger for handling errors in the DAO layer.
	 */
	private static final Logger LOGGER = Logger.getLogger(BorrowerService.class.getName());

	/**
	 * The DAO for the "books" table.
	 */
	@Autowired
	private BookDao bookDao;
	/**
	 * The currently-active transaction, or null if not in a transaction.
	 */
	private volatile TransactionStatus transaction;
	/**
	 * The transaction manager provided by Spring.
	 */
	@Autowired
	private PlatformTransactionManager transactionManager;

	@Override
	public void beginTransaction() throws TransactionException {
		if (transaction == null) {
			synchronized (this) {
				if (transaction == null) {
					transaction = transactionManager.getTransaction(null);
				}
			}
		}
	}

	/**
	 * To construct this service class, the caller must supply a clock to get "the
	 * current date."
	 *
	 * @param clock a clock (time-zone) to get "the current date".
	 */
	public BorrowerServiceImpl(final Clock clock) {
		this.clock = clock;
	}

	/**
	 * Constructor that uses the default time zone.
	 *
	 * @throws IOException  on I/O error reading DB configuration
	 * @throws SQLException on error setting up the database or DAOs
	 */
	public BorrowerServiceImpl() throws IOException, SQLException {
		this(Clock.systemDefaultZone());
	}

	@Override
	public List<Branch> getAllBranches() throws TransactionException {
		try {
			return branchDao.findAll();
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE,  "Error while getting all branches", except);
			throw rollback(new UnknownSQLException("Getting all branches failed", except));
		}
	}

	@Override
	public Loan borrowBook(final Borrower borrower, final Book book,
			final Branch branch, final LocalDateTime dateOut,
			final LocalDate dueDate) throws TransactionException {
		try {
			if (loanDao.get(book, borrower, branch) == null) {
				final int copies = copiesDao.getCopies(branch, book);
				if (copies > 0) {
					copiesDao.setCopies(branch, book, copies - 1);
					return loanDao.create(book, borrower, branch, dateOut, dueDate);
				} else {
					return null;
				}
			} else {
				return null; // TODO: Add getLoan() method to interface
			}
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while creating a loan record", except);
			throw rollback(new InsertException("Creating a loan failed", except));
		}
	}

	@Override
	public List<BranchCopies> getAllBranchCopies(final Branch branch)
			throws TransactionException {
		try {
			return copiesDao.getAllBranchCopies(branch);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "Error while getting branch copies", except);
			throw rollback(new UnknownSQLException("Getting branch copy records failed", except));
		}
	}

	@Override
	public Boolean returnBook(final Borrower borrower, final Book book,
			final Branch branch, final LocalDate dueDate) throws TransactionException {
		final Optional<Loan> loan;
		try {
			loan = Optional.ofNullable(loanDao.get(book, borrower, branch));
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting loan details", except);
			throw rollback(new UnknownSQLException("Getting loan details failed", except));
		}
		if (loan.isPresent()) {
			if (LocalDate.now(clock).isAfter(loan.get().getDueDate())) {
				return false;
			} else {
				try {
					final int copies = copiesDao.getCopies(branch, book);
					copiesDao.setCopies(branch, book, copies + 1);
				} catch (final DataAccessException except) {
					LOGGER.log(Level.SEVERE, "SQL error while incrementing copies on return", except);
					throw rollback(new UnknownSQLException("Incrementing copies on return failed", except));
				}
				try {
					loanDao.delete(loan.get());
				} catch (final DataAccessException except) {
					LOGGER.log(Level.SEVERE, "SQL error while removing a loan record", except);
					throw rollback(new DeleteException("Removing loan record failed", except));
				}
				return true;
			}
		} else {
			return null;
		}
	}

	@Override
	public List<Branch> getAllBranchesWithLoan(final Borrower borrower)
			throws TransactionException {
		return getAllBorrowedBooks(borrower).parallelStream().map(Loan::getBranch)
				.collect(Collectors.toList());
	}

	@Override
	public List<Loan> getAllBorrowedBooks(final Borrower borrower)
			throws TransactionException {
		try {
			return loanDao.findAll().parallelStream()
					.filter(loan -> borrower.equals(loan.getBorrower()))
					.collect(Collectors.toList());
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting loan records", except);
			throw rollback(new UnknownSQLException("Getting loan records failed", except));
		}
	}

	@Override
	public Borrower getBorrower(final int cardNo) throws TransactionException {
		try {
			return borrowerDao.findById(cardNo).orElse(null);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting borrower details", except);
			throw rollback(new UnknownSQLException("Getting borrower record failed", except));
		}
	}

	@Override
	public void commit() throws TransactionException {
		try {
			synchronized (this) {
				if (transaction != null) {
					transactionManager.commit(transaction);
					transaction = null;
				}
			}
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "Error of some kind while committing transaction", except);
			throw new UnknownSQLException("Committing the transaction failed", except);
		}
	}
	private <E extends Exception> E rollback(final E pending) {
		try {
			transactionManager.rollback(transaction);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "Further error while rolling back transaction", except);
			pending.addSuppressed(except);
		}
		synchronized (this) {
			transaction = null;
		}
		return pending;
	}

	@Override
	public Branch getBranch(final int branchId) throws TransactionException {
		try {
			foundbranch = branchDao.findById(branchId).orElse(null);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting a branch", except);
			throw rollback(new RetrieveException("Getting a branch failed", except));
		}
		return foundbranch;
	}

	@Override
	public Book getBook(final int bookId) throws TransactionException {
		Book foundbook = null;
		try {
			foundbook = bookDao.findById(bookId).get();
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting a book", except);
			throw rollback(new RetrieveException("Getting a book failed", except));
		}
		return foundbook;
	}

	@Override
	public Loan getLoan(final int cardNo, final int branchId, final int bookId) throws TransactionException {
		Loan foundLoan = null;
		try {
			foundLoan = loanDao.get(bookDao.findById(bookId).get(),
					borrowerDao.findById(cardNo).get(),
					branchDao.findById(branchId).get());
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting a Loan record", except);
			throw rollback(new RetrieveException("Getting a Loan failed", except));
		}
		return foundLoan;
	}
}
