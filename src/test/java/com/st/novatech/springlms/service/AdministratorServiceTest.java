package com.st.novatech.springlms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.st.novatech.springlms.dao.AuthorDao;
import com.st.novatech.springlms.dao.BookDao;
import com.st.novatech.springlms.dao.BookLoansDao;
import com.st.novatech.springlms.dao.BorrowerDao;
import com.st.novatech.springlms.dao.InMemoryAuthorDao;
import com.st.novatech.springlms.dao.InMemoryBookDao;
import com.st.novatech.springlms.dao.InMemoryBookLoansDao;
import com.st.novatech.springlms.dao.InMemoryBorrowerDao;
import com.st.novatech.springlms.dao.InMemoryLibraryBranchDao;
import com.st.novatech.springlms.dao.InMemoryPublisherDao;
import com.st.novatech.springlms.dao.LibraryBranchDao;
import com.st.novatech.springlms.dao.PublisherDao;
import com.st.novatech.springlms.exception.TransactionException;
import com.st.novatech.springlms.model.Author;
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Borrower;
import com.st.novatech.springlms.model.Branch;
import com.st.novatech.springlms.model.Loan;
import com.st.novatech.springlms.model.Publisher;

/**
 * Tests of the 'administrator service' implementation.
 *
 * @author Jonathan Lovelace
 *
 */
public final class AdministratorServiceTest {
	/**
	 * The (list-in-memory) library-branch DAO.
	 */
	private LibraryBranchDao branchDao;
	/**
	 * The (list-in-memory) book DAO.
	 */
	private BookDao bookDao;
	/**
	 * The (list-in-memory) author DAO.
	 */
	private AuthorDao authorDao;
	/**
	 * The (list-in-memory) publisher DAO.
	 */
	private PublisherDao publisherDao;
	/**
	 * The (list-in-memory) loans DAO.
	 */
	private BookLoansDao loansDao;
	/**
	 * The (list-in-memory) borrower DAO.
	 */
	private BorrowerDao borrowerDao;
	/**
	 * The service object under test.
	 */
	private AdministratorService testee;

	private static void noop() {
		// noop
	}

	/**
	 * Set up for each test.
	 */
	@BeforeEach
	public void setUp() {
		branchDao = new InMemoryLibraryBranchDao();
		bookDao = new InMemoryBookDao();
		authorDao = new InMemoryAuthorDao();
		publisherDao = new InMemoryPublisherDao();
		loansDao = new InMemoryBookLoansDao();
		borrowerDao = new InMemoryBorrowerDao();
		testee = new AdministratorServiceImpl(branchDao, bookDao, authorDao,
				publisherDao, loansDao, borrowerDao, AdministratorServiceTest::noop,
				AdministratorServiceTest::noop);
	}

	/**
	 * Test book creation.
	 *
	 * @throws TransactionException never
	 * @throws SQLException         never
	 */
	@Test
	public void testCreateBook() throws TransactionException, SQLException {
		final Book bookOne = new Book(1, "first title", null, null);
		final Author author = new Author(2, "author name");
		final Publisher publisher = new Publisher(3, "publisher name",
				"publisher address", "publisher phone");
		final Book bookTwo = new Book(2, "second title", author, publisher);
		assertEquals(bookOne, testee.createBook("first title", null, null),
				"createBook creates expected book");
		assertEquals(bookTwo, testee.createBook("second title", author, publisher),
				"createBook creates expected book");
		assertEquals(2, bookDao.getAll().size(),
				"After creating two books, two are stored");
	}

	/**
	 * Test book updating.
	 *
	 * @throws SQLException         never
	 * @throws TransactionException never
	 */
	@Test
	public void testUpdateBook() throws SQLException, TransactionException {
		final Book original = bookDao.create("original title", null, null);
		final Book replacement = new Book(1, "new title", null, null);
		testee.updateBook(replacement);
		assertEquals(replacement, bookDao.get(1),
				"replacement is now in data store");
		assertFalse(bookDao.getAll().contains(original),
				"original has vanished from data store");
	}

	/**
	 * Test book deletion.
	 *
	 * @throws SQLException         never
	 * @throws TransactionException never
	 */
	@Test
	public void testDeleteBook() throws SQLException, TransactionException {
		bookDao.create("first book", null, null);
		final Book toDelete = bookDao.create("second book", null, null);
		bookDao.create("third book", null, null);
		testee.deleteBook(toDelete);
		assertFalse(bookDao.getAll().contains(toDelete),
				"Deleted book is gone from data store");
		assertEquals(2, bookDao.getAll().size(),
				"data store row count is as expected");
	}

	/**
	 * Test getting all books.
	 *
	 * @throws SQLException         never
	 * @throws TransactionException never
	 */
	@Test
	public void testGetAllBooks() throws SQLException, TransactionException {
		final List<Book> expected = Arrays.asList(
				new Book(1, "first book", null, null),
				new Book(2, "second book", null, null),
				new Book(3, "third book", null, null));
		for (final Book book : expected) {
			bookDao.create(book.getTitle(), book.getAuthor(), book.getPublisher());
		}
		assertEquals(new HashSet<>(expected), new HashSet<>(testee.getAllBooks()),
				"getAllBooks returns expected data");
	}

	/**
	 * Test creating an author.
	 *
	 * @throws TransactionException never
	 * @throws SQLException         never
	 */
	@Test
	public void testCreateAuthor() throws TransactionException, SQLException {
		final Author authorOne = testee.createAuthor("author one");
		final Author authorTwo = testee.createAuthor("author two");
		final Author authorThree = testee.createAuthor("author three");
		assertEquals(new HashSet<>(Arrays.asList(authorOne, authorTwo, authorThree)),
				new HashSet<>(authorDao.getAll()),
				"Service-created authors are stored");
	}

	/**
	 * Test updating an author.
	 *
	 * @throws TransactionException never
	 * @throws SQLException         never
	 */
	@Test
	public void testUpdateAuthor() throws TransactionException, SQLException {
		final Author original = authorDao.create("original name");
		final Author replacement = new Author(1, "new name");
		testee.updateAuthor(replacement);
		assertEquals(replacement, authorDao.get(1),
				"replacement is now in data store");
		assertFalse(authorDao.getAll().contains(original),
				"original has vanished from data store");
	}

	/**
	 * Test deleting an author.
	 *
	 * @throws SQLException         never
	 * @throws TransactionException never
	 */
	@Test
	public void testDeleteAuthor() throws SQLException, TransactionException {
		authorDao.create("first author");
		final Author toDelete = authorDao.create("second author");
		authorDao.create("third author");
		testee.deleteAuthor(toDelete);
		assertFalse(authorDao.getAll().contains(toDelete),
				"Deleted author is gone from data store");
		assertEquals(2, authorDao.getAll().size(),
				"data store row count is as expected");
	}

	/**
	 * Test getting all authors.
	 *
	 * @throws SQLException         never
	 * @throws TransactionException never
	 */
	@Test
	public void testGetAllAuthors() throws SQLException, TransactionException {
		final List<Author> expected = Arrays.asList(new Author(1, "first author"),
				new Author(2, "second author"), new Author(3, "third author"));
		for (final Author author : expected) {
			authorDao.create(author.getName());
		}
		assertEquals(new HashSet<>(expected), new HashSet<>(testee.getAllAuthors()),
				"getAll returns expected");
	}

	/**
	 * Test the one-arg publisher-creation method.
	 *
	 * @throws TransactionException never
	 * @throws SQLException         never
	 */
	@Test
	public void testCreatePublisherString()
			throws TransactionException, SQLException {
		final Publisher expected = new Publisher(1, "publisher name", "", "");
		final Publisher actual = testee.createPublisher("publisher name");
		assertEquals(expected, actual, "createPublisher() creates expected result");
		assertEquals(expected, publisherDao.get(1),
				"createPublisher() stores to data store");
		assertEquals(1, publisherDao.getAll().size(),
				"createPublisher() doesn't create extras");
	}

	/**
	 * Test the three-arg publisher-creation method.
	 *
	 * @throws TransactionException never
	 * @throws SQLException         never
	 */
	@Test
	public void testCreatePublisherStringStringString()
			throws TransactionException, SQLException {
		testee.createPublisher("ignored");
		final Publisher expected = new Publisher(2, "publisher name",
				"publisher address", "publisher phone");
		final Publisher actual = testee.createPublisher("publisher name",
				"publisher address", "publisher phone");
		assertEquals(expected, actual, "expected publisher is created");
		assertEquals(expected, publisherDao.get(2),
				"createPublisher() stores to data store");
		assertEquals(2, publisherDao.getAll().size(),
				"createPublisher() doesn't create extras");
	}

	/**
	 * Test publisher updating.
	 *
	 * @throws SQLException         never
	 * @throws TransactionException never
	 */
	@Test
	public void testUpdatePublisher() throws SQLException, TransactionException {
		final Publisher original = publisherDao.create("original name",
				"original address", "original phone");
		final Publisher replacement = new Publisher(1, "new name", "new address",
				"new phone");
		testee.updatePublisher(replacement);
		assertEquals(replacement, publisherDao.get(1),
				"replacement now in data store");
		assertFalse(publisherDao.getAll().contains(original),
				"original gone from data store");
	}

	/**
	 * Test publisher deletion.
	 *
	 * @throws TransactionException never
	 * @throws SQLException         never
	 */
	@Test
	public void testDeletePublisher() throws TransactionException, SQLException {
		publisherDao.create("first publisher", "", "");
		final Publisher toDelete = publisherDao.create("second publisher", "", "");
		publisherDao.create("third publisher", "", "");
		testee.deletePublisher(toDelete);
		assertFalse(publisherDao.getAll().contains(toDelete),
				"deleted publisher gone from data store");
		assertEquals(2, publisherDao.getAll().size(),
				"data store row count as expected");
	}

	/**
	 * Test getting all publishers.
	 *
	 * @throws SQLException         never
	 * @throws TransactionException never
	 */
	@Test
	public void testGetAllPublishers() throws SQLException, TransactionException {
		final List<Publisher> expected = Arrays.asList(
				new Publisher(1, "first publisher", "first address", ""),
				new Publisher(2, "second publisher", "", "second phone"),
				new Publisher(3, "third publisher", "third address", "third phone"),
				new Publisher(4, "fourth publisher", "fourth address",
						"fourth phone"));
		for (final Publisher publisher : expected) {
			publisherDao.create(publisher.getName(), publisher.getAddress(),
					publisher.getPhone());
		}
		assertEquals(new HashSet<>(expected),
				new HashSet<>(testee.getAllPublishers()), "getAll returns expected");
	}

	/**
	 * Test branch creation.
	 *
	 * @throws SQLException         never
	 * @throws TransactionException never
	 */
	@Test
	public void testCreateBranch() throws SQLException, TransactionException {
		final Branch expected = new Branch(1, "branch name", "branch address");
		final Branch actual = testee.createBranch("branch name", "branch address");
		assertEquals(expected, actual, "createBranch() creates expected result");
		assertEquals(expected, branchDao.get(1),
				"createBranch() stores to data store");
		assertEquals(1, branchDao.getAll().size(),
				"createBranch() doesn't create extras");
	}

	/**
	 * Test branch deletion.
	 *
	 * @throws TransactionException never
	 * @throws SQLException         never
	 */
	@Test
	public void testDeleteBranch() throws TransactionException, SQLException {
		branchDao.create("first branch", "first address");
		final Branch toDelete = branchDao.create("second branch", "second address");
		branchDao.create("third branch", "third address");
		testee.deleteBranch(toDelete);
		assertFalse(branchDao.getAll().contains(toDelete),
				"deleted branch gone from data store");
		assertEquals(2, branchDao.getAll().size(),
				"data store row count as expected");
	}

	/**
	 * Test branch updating.
	 *
	 * @throws SQLException         never
	 * @throws TransactionException never
	 */
	@Test
	public void testUpdateBranch() throws SQLException, TransactionException {
		final Branch original = branchDao.create("orignal name", "original address");
		final Branch replacement = new Branch(1, "new name", "new address");
		testee.updateBranch(replacement);
		assertEquals(replacement, branchDao.get(1), "replacement now in data store");
		assertFalse(branchDao.getAll().contains(original),
				"original gone from data store");
	}

	/**
	 * Test borrower creation.
	 *
	 * @throws TransactionException never
	 * @throws SQLException         never
	 */
	@Test
	public void testCreateBorrower() throws TransactionException, SQLException {
		testee.createBorrower("ignored", "", "");
		testee.createBorrower("also ignored", "", "");
		final Borrower expected = new Borrower(3, "borrower name",
				"borrower address", "borrower phone");
		final Borrower actual = testee.createBorrower("borrower name",
				"borrower address", "borrower phone");
		assertEquals(expected, actual, "expected borrower is created");
		assertEquals(expected, borrowerDao.get(3),
				"createBorrower() stores to data store");
		assertEquals(3, borrowerDao.getAll().size(),
				"createBorrower() doesn't create extras");
	}

	/**
	 * Test borrower updating.
	 *
	 * @throws SQLException         never
	 * @throws TransactionException never
	 */
	@Test
	public void testUpdateBorrower() throws SQLException, TransactionException {
		final Borrower orignal = borrowerDao.create("original name",
				"original address", "original phone");
		final Borrower replacement = new Borrower(1, "new name", "new address",
				"new phone");
		testee.updateBorrower(replacement);
		assertEquals(replacement, borrowerDao.get(1),
				"replacement now in data store");
		assertFalse(borrowerDao.getAll().contains(orignal),
				"original gone from data store");
	}

	/**
	 * Test borrower deletion.
	 *
	 * @throws SQLException         never
	 * @throws TransactionException never
	 */
	@Test
	public void testDeleteBorrower() throws SQLException, TransactionException {
		borrowerDao.create("first borrower", "", "");
		borrowerDao.create("second borrower", "", "");
		final Borrower toDelete = borrowerDao.create("third borrower", "", "");
		borrowerDao.create("fourth borrower", "", "");
		testee.deleteBorrower(toDelete);
		assertFalse(borrowerDao.getAll().contains(toDelete),
				"deleted borrower gone from data store");
		assertEquals(3, borrowerDao.getAll().size(),
				"data store row count as expected");
	}

	/**
	 * Test getting all borrowers.
	 *
	 * @throws SQLException         never
	 * @throws TransactionException never
	 */
	@Test
	public void testGetAllBorrowers() throws SQLException, TransactionException {
		final List<Borrower> expected = Arrays.asList(
				new Borrower(1, "first borrower", "first address", ""),
				new Borrower(2, "second borrower", "", "second phone"),
				new Borrower(3, "third borrower", "third address", "third phone"));
		for (final Borrower borrower : expected) {
			borrowerDao.create(borrower.getName(), borrower.getAddress(),
					borrower.getPhone());
		}
		assertEquals(new HashSet<>(expected),
				new HashSet<>(testee.getAllBorrowers()), "getAll returns expected");
	}

	/**
	 * Test overriding loan due date.
	 *
	 * @throws SQLException         never
	 * @throws TransactionException never
	 */
	@Test
	public void testOverrideDueDateForLoan()
			throws SQLException, TransactionException {
		final Book book = new Book(1, "", null, null);
		final Borrower borrower = new Borrower(1, "", "", "");
		final Branch branch = new Branch(1, "", "");
		final LocalDateTime dateOut = LocalDateTime.now();
		final LocalDate dueDate = LocalDate.now();
		loansDao.create(book, borrower, branch, dateOut, dueDate);
		final LocalDate newDueDate = dueDate.plusMonths(3);
		final Loan expected = new Loan(book, borrower, branch, dateOut, newDueDate);
		testee.overrideDueDateForLoan(book, borrower, branch, newDueDate);
		assertEquals(expected, loansDao.get(book, borrower, branch),
				"Overriding due date works as expected");
	}

	/**
	 * Test getting all loans.
	 * @throws SQLException never
	 * @throws TransactionException never
	 */
	@Test
	public void testGetAllLoans() throws SQLException, TransactionException {
		final List<Loan> expected = Arrays.asList(new Loan(
				new Book(1, "", null, null), new Borrower(1, "", "", ""),
				new Branch(1, "", ""), LocalDateTime.now(), LocalDate.now()),
				new Loan(new Book(2, "", null, null), new Borrower(2, "", "", ""),
						new Branch(2, "", ""), LocalDateTime.now(),
						LocalDate.now()));
		for (final Loan loan : expected) {
			loansDao.create(loan.getBook(), loan.getBorrower(), loan.getBranch(),
					loan.getDateOut(), loan.getDueDate());
		}
		assertEquals(new HashSet<>(expected), new HashSet<>(testee.getAllLoans()),
				"getAll returns expected");
	}

	/**
	 * Test getting all branches.
	 * @throws SQLException never
	 * @throws TransactionException never
	 */
	@Test
	public void testGetAllBranches() throws SQLException, TransactionException {
		final List<Branch> expected = Arrays.asList(
				new Branch(1, "first branch", "first address"),
				new Branch(2, "second branch", "second address"),
				new Branch(3, "third branch", "third address"));
		for (final Branch branch : expected) {
			branchDao.create(branch.getName(), branch.getAddress());
		}
		assertEquals(new HashSet<>(expected), new HashSet<>(testee.getAllBranches()),
				"getAll returns expected");
	}

}
