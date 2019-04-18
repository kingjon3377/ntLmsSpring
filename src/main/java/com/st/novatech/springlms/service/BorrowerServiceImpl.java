package com.st.novatech.springlms.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.st.novatech.springlms.dao.BookLoansDao;
import com.st.novatech.springlms.dao.BookLoansDaoImpl;
import com.st.novatech.springlms.dao.BorrowerDao;
import com.st.novatech.springlms.dao.BorrowerDaoImpl;
import com.st.novatech.springlms.dao.CopiesDao;
import com.st.novatech.springlms.dao.CopiesDaoImpl;
import com.st.novatech.springlms.dao.DBConnectionFactory;
import com.st.novatech.springlms.dao.LibraryBranchDao;
import com.st.novatech.springlms.dao.LibraryBranchDaoImpl;
import com.st.novatech.springlms.exception.DeleteException;
import com.st.novatech.springlms.exception.InsertException;
import com.st.novatech.springlms.exception.TransactionException;
import com.st.novatech.springlms.exception.UnknownSQLException;
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Borrower;
import com.st.novatech.springlms.model.Branch;
import com.st.novatech.springlms.model.Loan;
import com.st.novatech.springlms.util.ThrowingRunnable;

/**
 * The "service" class to help UIs for borrowers.
 *
 * @author Jonathan Lovelace
 */
public final class BorrowerServiceImpl implements BorrowerService {
	/**
	 * The DAO for the "branches" table.
	 */
	private final LibraryBranchDao branchDao;
	/**
	 * The DAO for the "loans" table.
	 */
	private final BookLoansDao loanDao;
	/**
	 * The DAO for the "copies" table.
	 */
	private final CopiesDao copiesDao;
	/**
	 * The DAO for the "borrowers" table.
	 */
	private final BorrowerDao borrowerDao;
	/**
	 * The clock to get "the current time" from.
	 */
	private final Clock clock;
	/**
	 * Logger for handling errors in the DAO layer.
	 */
	private static final Logger LOGGER = Logger.getLogger(BorrowerService.class.getName());
	/**
	 * Method to use to commit a transaction, if the DAO backend supports transactions.
	 */
	private final ThrowingRunnable<SQLException> commitHandle;
	/**
	 * Method to use to roll back a transaction, if the DAO backend supports transactions.
	 */
	private final ThrowingRunnable<SQLException> rollbackHandle;

	/**
	 * To construct this service class, the caller must supply instances of each DAO
	 * it uses, a clock to get "the current date," and method references to commit
	 * and roll back transactions.
	 *
	 * @param branchDao   the library-branch DAO.
	 * @param loanDao     the loan DAO
	 * @param copiesDao   the copies DAO
	 * @param borrowerDao the borrower DAO
	 * @param commit       the method handle to commit a transaction, if the backend
	 *                     supports that
	 * @param rollback     the method handle to roll back a transaction, if the
	 *                     backend supports that
	 */
	public BorrowerServiceImpl(final LibraryBranchDao branchDao,
			final BookLoansDao loanDao, final CopiesDao copiesDao,
			final BorrowerDao borrowerDao, final Clock clock,
			final ThrowingRunnable<SQLException> commit,
			final ThrowingRunnable<SQLException> rollback) {
		this.branchDao = branchDao;
		this.loanDao = loanDao;
		this.copiesDao = copiesDao;
		this.borrowerDao = borrowerDao;
		this.clock = clock;
		commitHandle = commit;
		rollbackHandle = rollback;
	}
	/**
	 * To construct this service class using this constructor, the caller must
	 * merely supply a connection to the database.
	 * @param db the connection to the database
	 * @throws SQLException on error setting up DAOs.
	 */
	public BorrowerServiceImpl(final Connection db) throws SQLException {
		this(new LibraryBranchDaoImpl(db), new BookLoansDaoImpl(db),
				new CopiesDaoImpl(db), new BorrowerDaoImpl(db),
				Clock.systemDefaultZone(), db::commit, db::rollback);
	}
	/**
	 * Constructor that uses the default DB connection factory to supply the
	 * database connection and uses the default DAO implementations.
	 * @throws IOException on I/O error reading DB configuration
	 * @throws SQLException on error setting up the database or DAOs
	 */
	public BorrowerServiceImpl() throws IOException, SQLException {
		this(DBConnectionFactory.getDatabaseConnection());
	}

	@Override
	public List<Branch> getAllBranches() throws TransactionException {
		try {
			return branchDao.getAll();
		} catch (final SQLException except) {
			LOGGER.log(Level.SEVERE,  "SQL error while getting all branches", except);
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
		} catch (final SQLException except) {
			LOGGER.log(Level.SEVERE, "SQL error while creating a loan record", except);
			throw rollback(new InsertException("Creating a loan failed", except));
		}
	}

	@Override
	public Map<Book, Integer> getAllBranchCopies(final Branch branch)
			throws TransactionException {
		try {
			return copiesDao.getAllBranchCopies(branch);
		} catch (final SQLException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting branch copies", except);
			throw rollback(new UnknownSQLException("Getting branch copy records failed", except));
		}
	}

	@Override
	public Boolean returnBook(final Borrower borrower, final Book book,
			final Branch branch, final LocalDate dueDate) throws TransactionException {
		final Optional<Loan> loan;
		try {
			loan = Optional.ofNullable(loanDao.get(book, borrower, branch));
		} catch (final SQLException except) {
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
				} catch (final SQLException except) {
					LOGGER.log(Level.SEVERE, "SQL error while incrementing copies on return", except);
					throw rollback(new UnknownSQLException("Incrementing copies on return failed", except));
				}
				try {
					loanDao.delete(loan.get());
				} catch (final SQLException except) {
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
			return loanDao.getAll().parallelStream()
					.filter(loan -> borrower.equals(loan.getBorrower()))
					.collect(Collectors.toList());
		} catch (final SQLException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting loan records", except);
			throw rollback(new UnknownSQLException("Getting loan records failed", except));
		}
	}

	@Override
	public Borrower getBorrower(final int cardNo) throws TransactionException {
		try {
			return borrowerDao.get(cardNo);
		} catch (final SQLException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting borrower details", except);
			throw rollback(new UnknownSQLException("Getting borrower record failed", except));
		}
	}
	@Override
	public void commit() throws TransactionException {
		try {
			commitHandle.run();
		} catch (final SQLException except) {
			LOGGER.log(Level.SEVERE, "Error of some kind while committing transaction", except);
			throw new UnknownSQLException("Committing the transaction failed", except);
		}
	}
	private <E extends Exception> E rollback(final E pending) {
		try {
			rollbackHandle.run();
		} catch (final SQLException except) {
			LOGGER.log(Level.SEVERE, "Further error while rolling back transaction", except);
			pending.addSuppressed(except);
		}
		return pending;
	}
}
