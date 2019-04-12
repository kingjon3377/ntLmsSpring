package com.st.novatech.springlms.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.st.novatech.springlms.exception.RetrieveException;
import com.st.novatech.springlms.exception.TransactionException;
import com.st.novatech.springlms.model.Author;
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Publisher;
import com.st.novatech.springlms.service.AdministratorService;

/**
 * Controller for cataloging administrators. FIXME: Limit access to most of these endpoints to authorized users
 * @author Jonathan Lovelace
 */
@RestController
public final class CatalogController {
	/**
	 * Service class used to handle requests.
	 */
	@Autowired
	private AdministratorService service;

	/**
	 * Get all authors from the database. Spring turns the list into JSON (or XML?).
	 * @return the list of all authors in the database.
	 * @throws TransactionException on internal error.
	 */
	@RequestMapping({"/authors", "/authors/"})
	public List<Author> getAuthors() throws TransactionException {
		return service.getAllAuthors();
	}

	/**
	 * Get all books from the database. Spring turns the list into JSON (or XML?).
	 * @return the list of all books in the database
	 * @throws TransactionException on internal error
	 */
	@RequestMapping({"/books", "/books/"})
	public List<Book> getBooks() throws TransactionException {
		return service.getAllBooks();
	}

	/**
	 * Get all publishers from the database. Spring turns the list into JSON (or XML?).
	 * @return the list of all publishers in the database
	 * @throws TransactionException on internal error
	 */
	@RequestMapping({"/publishers","/publishers/"})
	public List<Publisher> getPublishers() throws TransactionException {
		return service.getAllPublishers();
	}

	/**
	 * Get an author by its ID number.
	 * @param authorId the ID number of the author
	 * @return the author
	 * @throws TransactionException if author not found, or on internal error
	 */
	@RequestMapping({"/author/{authorId}", "/author/{authorId}/"})
	public Author getAuthor(@PathVariable("authorId") final int authorId) throws TransactionException {
		final Author author = service.getAuthor(authorId);
		if (author == null) {
			throw new RetrieveException("Author not found");
		} else {
			return author;
		}
	}

	/**
	 * Get a book by its ID number.
	 * @param bookId the ID number of the book
	 * @return the book
	 * @throws TransactionException if book not found, or on internal error
	 */
	@RequestMapping({"/book/{bookId}","/book/{bookId}/"})
	public Book getBook(@PathVariable("bookId") final int bookId) throws TransactionException {
		final Book book = service.getBook(bookId);
		if (book == null) {
			throw new RetrieveException("Book not found");
		} else {
			return book;
		}
	}

	/**
	 * Update an author by ID number.
	 * @param authorId the ID number of the author to update
	 * @param input the author data to update.
	 * @return the updated author
	 * @throws TransactionException if author not found, or on internal error
	 */
	@RequestMapping(path = { "/author/{authorId}",
			"/author/{authorId}/" }, method = RequestMethod.PUT)
	public Author updateAuthor(@PathVariable("authorId") final int authorId,
			@RequestBody final Author input) throws TransactionException {
		final Author author = service.getAuthor(authorId);
		if (author == null) {
			throw new RetrieveException("Author not found");
		} else {
			author.setName(input.getName());
			service.updateAuthor(author);
			return service.getAuthor(authorId);
		}
	}

	/**
	 * Create an author with the given name.
	 * @param name the name to give the author
	 * @return the created author
	 * @throws TransactionException on internal error
	 */
	@RequestMapping(path = {"/author", "/author/" }, method = RequestMethod.POST)
	public Author createAuthor(@RequestParam("name") final String name) throws TransactionException {
		return service.createAuthor(name);
	}

	/**
	 * Delete the author with the given ID.
	 * @param authorId the ID of the author to delete.
	 * @throws TransactionException on internal error
	 */
	@RequestMapping(path = {"/author/{authorId}", "/author/{authorId}"}, method = RequestMethod.DELETE)
	public void deleteAuthor(@PathVariable("authorId") final int authorId) throws TransactionException {
		final Author author = service.getAuthor(authorId);
		if (author != null) {
			service.deleteAuthor(author);
		}
	}
}
