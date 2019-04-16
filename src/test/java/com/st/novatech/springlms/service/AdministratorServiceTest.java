package com.st.novatech.springlms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.st.novatech.springlms.dao.AuthorDao;
import com.st.novatech.springlms.dao.BookDao;
import com.st.novatech.springlms.dao.BookLoansDao;
import com.st.novatech.springlms.dao.BorrowerDao;
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
@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public final class AdministratorServiceTest {
	/**
	 * Library-branch DAO used in tests.
	 */
	@Autowired
	private LibraryBranchDao branchDao;
	/**
	 * Book DAO used in tests.
	 */
	@Autowired
	private BookDao bookDao;
	/**
	 * Author DAO used in tests.
	 */
	@Autowired
	private AuthorDao authorDao;
	/**
	 * Publisher DAO used in tests.
	 */
	@Autowired
	private PublisherDao publisherDao;
	/**
	 * Loans DAO used in tests.
	 */
	@Autowired
	private BookLoansDao loansDao;
	/**
	 * Borrower DAO used in tests.
	 */
	@Autowired
	private BorrowerDao borrowerDao;
	/**
	 * The service object under test.
	 */
	@Autowired
	private AdministratorService testee;

	/**
	 * Test book creation.
	 *
	 * @throws TransactionException never
	 * @throws SQLException         never
	 */
	@Test
	public void testCreateBook() throws TransactionException, SQLException {
		final Book bookOne = new Book(1, "first title", null, null);
		final Author author = authorDao.create("author name");
		final Publisher publisher = publisherDao.create("publisher name",
				"publisher address", "publisher phone");
		final Book bookTwo = new Book(2, "second title", author, publisher);
		assertEquals(bookOne, testee.createBook("first title", null, null),
				"createBook creates expected book");
		assertEquals(bookTwo, testee.createBook("second title", author, publisher),
				"createBook creates expected book");
		assertEquals(2, bookDao.findAll().size(),
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
		assertEquals(replacement, bookDao.findById(1).get(),
				"replacement is now in data store");
		assertFalse(bookDao.findAll().contains(original),
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
		assertFalse(bookDao.findAll().contains(toDelete),
				"Deleted book is gone from data store");
		assertEquals(2, bookDao.findAll().size(),
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
				new HashSet<>(authorDao.findAll()),
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
		assertEquals(replacement, authorDao.findById(1).get(),
				"replacement is now in data store");
		assertFalse(authorDao.findAll().contains(original),
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
		assertFalse(authorDao.findAll().contains(toDelete),
				"Deleted author is gone from data store");
		assertEquals(2, authorDao.findAll().size(),
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
		assertEquals(expected, publisherDao.findById(1).get(),
				"createPublisher() stores to data store");
		assertEquals(1, publisherDao.findAll().size(),
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
		assertEquals(expected, publisherDao.findById(2).get(),
				"createPublisher() stores to data store");
		assertEquals(2, publisherDao.findAll().size(),
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
		testee.commit();
		assertEquals(replacement, publisherDao.findById(1).get(),
				"replacement now in data store");
		assertFalse(publisherDao.findAll().contains(original),
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
		assertFalse(publisherDao.findAll().contains(toDelete),
				"deleted publisher gone from data store");
		assertEquals(2, publisherDao.findAll().size(),
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
		assertEquals(expected, branchDao.findById(1).get(),
				"createBranch() stores to data store");
		assertEquals(1, branchDao.findAll().size(),
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
		assertFalse(branchDao.findAll().contains(toDelete),
				"deleted branch gone from data store");
		assertEquals(2, branchDao.findAll().size(),
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
		testee.commit();
		assertEquals(replacement, branchDao.findById(1).get(), "replacement now in data store");
		assertFalse(branchDao.findAll().contains(original),
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
		assertEquals(expected, borrowerDao.findById(3).get(),
				"createBorrower() stores to data store");
		assertEquals(3, borrowerDao.findAll().size(),
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
		assertEquals(replacement, borrowerDao.findById(1).get(),
				"replacement now in data store");
		assertFalse(borrowerDao.findAll().contains(orignal),
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
		assertFalse(borrowerDao.findAll().contains(toDelete),
				"deleted borrower gone from data store");
		assertEquals(3, borrowerDao.findAll().size(),
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
		final Book book = bookDao.create("", null, null);
		final Borrower borrower = borrowerDao.create("", "", "");
		final Branch branch = branchDao.create("", "");
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
		final List<Loan> expected = Arrays.asList(
				new Loan(bookDao.create("", null, null),
						borrowerDao.create("", "", ""), branchDao.create("", ""),
						LocalDateTime.now(), LocalDate.now()),
				new Loan(bookDao.create("", null, null),
						borrowerDao.create("", "", ""), branchDao.create("", ""),
						LocalDateTime.now(), LocalDate.now()));
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

	/**
	 * Test that retrieval works.
	 * @throws SQLException on database error
	 */
	@Test
	public void testGetBorrower() throws SQLException {
		final Borrower b = borrowerDao.create("Ibn Khaldoun", "ADR45", "PHN45");

		assertEquals(b.getName(), borrowerDao.findById(b.getCardNo()).get().getName(),
				"borrower has expected name");
	}

	/**
	 * Test that retrieving an author works.
	 * @throws SQLException on database error
	 */
	@Test
	public void testGetAuthor() throws SQLException {
		final Author a = authorDao.create("Ibn Khaldoun");
		assertEquals(a.getName(), authorDao.findById(a.getId()).get().getName(),
				"retrieved author has expected name");
	}

	/**
	 * Test that getting a book works.
	 * @throws SQLException on database error
	 */
	@Test
	public void testGetBook() throws SQLException {
		final Author foundAuthor = authorDao.findById(1).orElse(null);
		final Publisher foundPublisher = publisherDao.findById(1).orElse(null);
		final Book foundBook = bookDao.create("50 down", foundAuthor, foundPublisher);

		assertEquals(foundBook.getTitle(), bookDao.findById(foundBook.getId()).get().getTitle(),
				"retrieved book has expected title");
	}

	/**
	 * Test that getting a branch works.
	 * @throws SQLException on database error
	 */
	@Test
	public void testGetBranch() throws SQLException {
		final Branch p = branchDao.create("Branch 1457", "ADR45");
		assertEquals(p.getName(), branchDao.findById(p.getId()).get().getName(),
				"retrieved branch has expected name");
	}
	/**
	 * Test that retrieving a publisher works.
	 * @throws SQLException on database error
	 */
	@Test
	public void testGetPublisher() throws SQLException {
		final Publisher foundPublisher = publisherDao.create("Ibn Khaldoun", "ADR45", "PHN45");
		assertEquals(foundPublisher, publisherDao.findById(foundPublisher.getId()).get(),
				"publisher has expected name");
	}

}
