package com.st.novatech.springlms.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import com.st.novatech.springlms.dao.AuthorDao;
import com.st.novatech.springlms.dao.BookDao;
import com.st.novatech.springlms.dao.BookLoansDao;
import com.st.novatech.springlms.dao.BorrowerDao;
import com.st.novatech.springlms.dao.LibraryBranchDao;
import com.st.novatech.springlms.dao.PublisherDao;
import com.st.novatech.springlms.exception.DeleteException;
import com.st.novatech.springlms.exception.InsertException;
import com.st.novatech.springlms.exception.RetrieveException;
import com.st.novatech.springlms.exception.TransactionException;
import com.st.novatech.springlms.exception.UnknownSQLException;
import com.st.novatech.springlms.exception.UpdateException;
import com.st.novatech.springlms.model.Author;
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Borrower;
import com.st.novatech.springlms.model.Branch;
import com.st.novatech.springlms.model.Loan;
import com.st.novatech.springlms.model.Publisher;

/**
 * An implementation of the service class for administrative UIs.
 *
 * <p>TODO: Split interface into "cataloger" and "executive" services.
 *
 * @author Jonathan Lovelace
 *
 */
@Service("AdministratorService")
public final class AdministratorServiceImpl implements AdministratorService {
	/**
	 * DAO to access the library-branch table.
	 */
	@Autowired
	private LibraryBranchDao branchDao;
	/**
	 * DAO to access the book table.
	 */
	@Autowired
	private BookDao bookDao;
	/**
	 * DAO to access the author table.
	 */
	@Autowired
	private AuthorDao authorDao;
	/**
	 * DAO to access the publisher table.
	 */
	@Autowired
	private PublisherDao publisherDao;
	/**
	 * DAO to access the borrower table.
	 */
	@Autowired
	private BorrowerDao borrowerDao;
	/**
	 * DAO to access the loans table.
	 */
	@Autowired
	private BookLoansDao loansDao;
	/**
	 * Logger for handling errors in the DAO layer.
	 */
	private static final Logger LOGGER = Logger.getLogger(AdministratorService.class.getName());

	/**
	 * The currently-active transaction, or null if not in a transaction.
	 */
	private TransactionStatus transaction;
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

	@Override
	public List<Branch> getAllBranches() throws TransactionException {
		try {
			return branchDao.findAll();
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE,  "SQL error while getting all branches", except);
			throw rollback(new UnknownSQLException("Getting all branches failed", except));
		}
	}

	@Override
	public Book createBook(final String title, final Author author,
			final Publisher publisher) throws TransactionException {
		try {
			return bookDao.create(title, author, publisher);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while creating a book", except);
			throw rollback(new InsertException("Creating a book failed", except));
		}
	}

	@Override
	public void updateBook(final Book book) throws TransactionException {
		try {
			bookDao.save(book);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while updating a book", except);
			throw rollback(new UpdateException("Updating book record failed", except));
		}
	}

	@Override
	public void deleteBook(final Book book) throws TransactionException {
		try {
			bookDao.delete(book);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while removing a book record", except);
			throw rollback(new DeleteException("Removing book record failed", except));
		}
	}

	@Override
	public List<Book> getAllBooks() throws TransactionException {
		try {
			return bookDao.findAll();
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting books", except);
			throw rollback(new UnknownSQLException("Getting book records failed", except));
		}
	}

	@Override
	public Author createAuthor(final String name) throws TransactionException {
		try {
			return authorDao.create(name);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while creating an author", except);
			throw rollback(new InsertException("Creating an author failed", except));
		}

	}

	@Override
	public void updateAuthor(final Author author) throws TransactionException {
		try {
			authorDao.save(author);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while updating an author", except);
			throw rollback(new UpdateException("Updating author record failed", except));
		}
	}

	@Override
	public void deleteAuthor(final Author author) throws TransactionException {
		try {
			authorDao.delete(author);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while removing an author record", except);
			throw rollback(new DeleteException("Removing author record failed", except));
		}
	}

	@Override
	public List<Author> getAllAuthors() throws TransactionException {
		try {
			return authorDao.findAll();
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting authors", except);
			throw rollback(new UnknownSQLException("Getting author records failed", except));
		}
	}

	@Override
	public Publisher createPublisher(final String name) throws TransactionException {
		return createPublisher(name, "", "");
	}

	@Override
	public Publisher createPublisher(final String name, final String address,
			final String phone) throws TransactionException {
		try {
			return publisherDao.create(name, address, phone);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while creating a publisher", except);
			throw rollback(new InsertException("Creating a publisher failed", except));
		}
	}

	@Override
	public void updatePublisher(final Publisher publisher) throws TransactionException {
		try {
			publisherDao.save(publisher);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while updating a publisher", except);
			throw rollback(new UpdateException("Updating publisher record failed", except));
		}
	}

	@Override
	public void deletePublisher(final Publisher publisher) throws TransactionException {
		try {
			publisherDao.delete(publisher);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while removing a publisher record", except);
			throw rollback(new DeleteException("Removing publisher record failed", except));
		}
	}

	@Override
	public List<Publisher> getAllPublishers() throws TransactionException {
		try {
			return publisherDao.findAll();
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting publishers", except);
			throw rollback(new UnknownSQLException("Getting publisher records failed", except));
		}
	}

	@Override
	public Branch createBranch(final String name, final String address) throws TransactionException {
		try {
			return branchDao.create(name, address);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while creating a branch", except);
			throw rollback(new InsertException("Creating a branch failed", except));
		}
	}

	@Override
	public void deleteBranch(final Branch branch) throws TransactionException {
		try {
			branchDao.delete(branch);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while removing a branch record", except);
			throw rollback(new DeleteException("Removing branch record failed", except));
		}
	}

	@Override
	public void updateBranch(final Branch branch) throws TransactionException {
		try {
			branchDao.save(branch);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while updating a branch", except);
			throw rollback(new UpdateException("Updating branch record failed", except));
		}
	}

	@Override
	public Borrower createBorrower(final String name, final String address,
			final String phone) throws TransactionException {
		try {
			return borrowerDao.create(name, address, phone);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while creating a borrower", except);
			throw rollback(new InsertException("Creating a borrower failed", except));
		}
	}

	@Override
	public void updateBorrower(final Borrower borrower) throws TransactionException {
		try {
			borrowerDao.save(borrower);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while updating a borrower", except);
			throw rollback(new UpdateException("Updating borrower record failed", except));
		}
	}

	@Override
	public void deleteBorrower(final Borrower borrower) throws TransactionException {
		try {
			borrowerDao.delete(borrower);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while removing a borrower record", except);
			throw rollback(new DeleteException("Removing borrower record failed", except));
		}
	}

	@Override
	public List<Borrower> getAllBorrowers() throws TransactionException {
		try {
			return borrowerDao.findAll();
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting borrowers", except);
			throw rollback(new UnknownSQLException("Getting borrower records failed", except));
		}
	}

	@Override
	public boolean overrideDueDateForLoan(final Book book, final Borrower borrower,
			final Branch branch, final LocalDate dueDate) throws TransactionException {
		final Optional<Loan> loan;
		try {
			loan = Optional.ofNullable(loansDao.get(book, borrower, branch));
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting loan record", except);
			throw rollback(new UnknownSQLException("Getting loan record failed", except));
		}
		if (loan.isPresent()) {
			loan.get().setDueDate(dueDate);
			try {
				loansDao.save(loan.get());
			} catch (final DataAccessException except) {
				LOGGER.log(Level.SEVERE, "SQL error while updating a loan", except);
				throw rollback(new UpdateException("Updating loan record failed", except));
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<Loan> getAllLoans() throws TransactionException {
		try {
			return loansDao.findAll();
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting loans", except);
			throw rollback(new UnknownSQLException("Getting loan records failed", except));
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
			if (transaction != null) {
				transactionManager.rollback(transaction);
			}
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
	public Borrower getBorrower(final int cardNo) throws TransactionException {
		try {
			return borrowerDao.findById(cardNo).orElse(null);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting borrower details", except);
			throw rollback(new RetrieveException("Getting borrower record failed", except));
		}
	}

	@Override
	public Author getAuthor(final int authorId) throws TransactionException {
		try {
			return authorDao.findById(authorId).orElse(null);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting borrower details", except);
			throw rollback(new RetrieveException("Getting borrower record failed", except));
		}
	}

	@Override
	public Publisher getPublisher(final int publisherId) throws TransactionException {
		try {
			return publisherDao.findById(publisherId).orElse(null);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting borrower details", except);
			throw rollback(new RetrieveException("Getting borrower record failed", except));
		}
	}

	@Override
	public Branch getbranch(final int branchId) throws TransactionException {
		try {
			return branchDao.findById(branchId).orElse(null);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting a branch", except);
			throw rollback(new RetrieveException("Getting a branch failed", except));
		}
	}

	@Override
	public Book getBook(final int bookId) throws TransactionException {
		try {
			return bookDao.findById(bookId).orElse(null);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting a book", except);
			throw rollback(new RetrieveException("Getting a book failed", except));
		}
	}
	@Override
	public Loan getLoan(final int cardNo, final int branchId, final int bookId) throws TransactionException {
		try {
			return loansDao.get(getBook(bookId), getBorrower(cardNo), getbranch(branchId));
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting a Loan record", except);
			throw rollback(new RetrieveException("Getting a Loan failed", except));
		}
	}
}
