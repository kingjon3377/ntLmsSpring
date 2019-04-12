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

	@RequestMapping({"/authors", "/authors/"})
	public List<Author> getAuthor() throws TransactionException {
		return service.getAllAuthors();
	}

	@RequestMapping({"/books", "/books/"})
	public List<Book> getBooks() throws TransactionException {
		return service.getAllBooks();
	}

	@RequestMapping({"/author/${uthorId}", "/author/{authorId}/"})
	public Author getAuthor(@PathVariable("authorId") final int authorId) throws TransactionException {
		final Author author = service.getAuthor(authorId);
		if (author == null) {
			throw new RetrieveException("Author not found");
		} else {
			return author;
		}
	}

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

	@RequestMapping(path = {"/author", "/author/" }, method = RequestMethod.POST)
	public Author createAuthor(@RequestParam("name") final String name) throws TransactionException {
		return service.createAuthor(name);
	}

	@RequestMapping(path = {"/author/{authorId}", "/author/{authorId}"}, method = RequestMethod.DELETE)
	public void deleteAuthor(@PathVariable("authorId") final int authorId) throws TransactionException {
		final Author author = service.getAuthor(authorId);
		if (author != null) {
			service.deleteAuthor(author);
		}
	}
}
