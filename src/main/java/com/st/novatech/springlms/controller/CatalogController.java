package com.st.novatech.springlms.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

	@RequestMapping(path = { "/author/{authorId}",
			"/author/{authorId}/" }, method = RequestMethod.PUT)
	public Author updateAuthor(@PathVariable("authorId") final int authorId,
			@RequestBody final Author input) throws TransactionException {
		final Author author = new Author(authorId, input.getName());
		service.updateAuthor(author); // TODO: what if no author with that ID yet? Should create ...
		// TODO: get author by ID rather than returning user input
		return author;
	}

	@RequestMapping(path = {"/author", "/author/" }, method = RequestMethod.POST)
	public Author createAuthor(@RequestParam("name") final String name) throws TransactionException {
		return service.createAuthor(name);
	}
}
