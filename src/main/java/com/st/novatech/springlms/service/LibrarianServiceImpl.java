package com.st.novatech.springlms.service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import com.st.novatech.springlms.dao.BookDao;
import com.st.novatech.springlms.dao.CopiesDao;
import com.st.novatech.springlms.dao.LibraryBranchDao;
import com.st.novatech.springlms.exception.RetrieveException;
import com.st.novatech.springlms.exception.TransactionException;
import com.st.novatech.springlms.exception.UnknownSQLException;
import com.st.novatech.springlms.exception.UpdateException;
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Branch;
import com.st.novatech.springlms.model.BranchCopies;

/**
 * The "service" class to help UIs for librarians.
 *
 * @author Jonathan Lovelace
 */
@Service
public final class LibrarianServiceImpl implements LibrarianService {
	/**
	 * The DAO for the "branches" table.
	 */
	@Autowired
	private LibraryBranchDao branchDao;
	/**
	 * The DAO for the "books" table.
	 */
	@Autowired
	private BookDao bookDao;
	/**
	 * The DAO for the "copies" table.
	 */
	@Autowired
	private CopiesDao copiesDao;
	/**
	 * Logger for handling errors in the DAO layer.
	 */
	private static final Logger LOGGER = Logger.getLogger(LibrarianService.class.getName());
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
	public void updateBranch(final Branch branch) throws TransactionException {
		try {
			branchDao.save(branch);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while updating a book", except);
			throw rollback(new UpdateException("Updating book record failed", except));
		}
	}

	@Override
	public void setBranchCopies(final Branch branch, final Book book,
			final int noOfCopies) throws TransactionException {
		try {
			copiesDao.setCopies(branch, book, noOfCopies);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while setting copy records", except);
			throw rollback(new UnknownSQLException("Setting copy records failed", except));
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
	public List<BranchCopies> getAllCopies() throws TransactionException {
		try {
			return copiesDao.getAllCopies();
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting copy records", except);
			throw rollback(new UnknownSQLException("Getting copy records failed", except));
		}
	}

	@Override
	public Branch getbranch(final int branchId) throws TransactionException {
		Branch foundbranch = null;
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
			foundbook = bookDao.findById(bookId).orElse(null);
		} catch (final DataAccessException except) {
			LOGGER.log(Level.SEVERE, "SQL error while getting a book", except);
			throw rollback(new RetrieveException("Getting a book failed", except));
		}
		return foundbook;
	}

	@Override
	public void commit() throws TransactionException {
		try {
			synchronized (this) {
				transactionManager.commit(transaction);
				transaction = null;
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
	public int getCopies(final Book book, final Branch branch) throws TransactionException {
		return copiesDao.getCopies(branch, book);
	}
}
