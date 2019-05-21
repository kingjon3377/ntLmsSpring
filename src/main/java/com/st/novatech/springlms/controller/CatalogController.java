package com.st.novatech.springlms.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.st.novatech.springlms.exception.RetrieveException;
import com.st.novatech.springlms.exception.TransactionException;
import com.st.novatech.springlms.model.Author;
import com.st.novatech.springlms.model.Book;
import com.st.novatech.springlms.model.Publisher;
import com.st.novatech.springlms.service.AdministratorService;

/**
 * Controller for cataloging administrators.
 *
 * <p>FIXME: Limit access to most of these endpoints to authorized users
 *
 * <p>FIXME: Commit after making changes to the database.
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
	@GetMapping({"/authors", "/authors/"})
	public List<Author> getAuthors() throws TransactionException {
		return service.getAllAuthors();
	}

	/**
	 * Get all books from the database. Spring turns the list into JSON (or XML?).
	 * @return the list of all books in the database
	 * @throws TransactionException on internal error
	 */
	// TODO: Uncomment once controllers are split for service-discovery refactoring
//	@GetMapping({"/books", "/books/"})
	public List<Book> getBooks() throws TransactionException {
		return service.getAllBooks();
	}

	/**
	 * Get all publishers from the database. Spring turns the list into JSON (or XML?).
	 * @return the list of all publishers in the database
	 * @throws TransactionException on internal error
	 */
	@GetMapping({"/publishers","/publishers/"})
	public List<Publisher> getPublishers() throws TransactionException {
		return service.getAllPublishers();
	}

	/**
	 * Get an author by its ID number.
	 * @param authorId the ID number of the author
	 * @return the author
	 * @throws TransactionException if author not found, or on internal error
	 */
	@GetMapping({"/author/{authorId}", "/author/{authorId}/"})
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
	// TODO: Uncomment once controllers are split for service-discovery refactoring
//	@GetMapping({"/book/{bookId}","/book/{bookId}/"})
	public Book getBook(@PathVariable("bookId") final int bookId) throws TransactionException {
		final Book book = service.getBook(bookId);
		if (book == null) {
			throw new RetrieveException("Book not found");
		} else {
			return book;
		}
	}

	/**
	 * Get a publisher by ID number.
	 * @param publisherId the ID number of the publisher
	 * @return the publisher
	 * @throws TransactionException if publisher not found, or on internal error
	 */
	@GetMapping({"/publisher/{publisherId}", "/publisher/{publisherId}/"})
	public Publisher getPublisher(@PathVariable("publisherId") final int publisherId)
			throws TransactionException {
		final Publisher publisher = service.getPublisher(publisherId);
		if (publisher == null) {
			throw new RetrieveException("Publisher not found");
		} else {
			return publisher;
		}
	}

	/**
	 * Update an author by ID number.
	 * @param authorId the ID number of the author to update
	 * @param input the author data to update.
	 * @return the updated author
	 * @throws TransactionException if author not found, or on internal error
	 */
	@PutMapping({ "/author/{authorId}", "/author/{authorId}/" })
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
	 * Update a publisher by ID number.
	 * @param publisherId the ID number of the publisher to update
	 * @param input the publisher data to update.
	 * @return the updated publisher
	 * @throws TransactionException if publisher not found, or on internal error
	 */
	@PutMapping({ "/publisher/{publisherId}", "/publisher/{publisherId}/" })
	public Publisher updatePublisher(
			@PathVariable("publisherId") final int publisherId,
			@RequestBody final Publisher input) throws TransactionException {
		final Publisher publisher = service.getPublisher(publisherId);
		if (publisher == null) {
			throw new RetrieveException("Publisher not found");
		} else {
			publisher.setName(input.getName());
			publisher.setAddress(input.getAddress());
			publisher.setPhone(input.getPhone());
			service.updatePublisher(publisher);
			return service.getPublisher(publisherId);
		}
	}

	/**
	 * Update a book by ID number. If author or publisher is null in the supplied
	 * data, the existing author or publisher is left alone. If the author or
	 * publisher has an ID that is not found in the database, an exception is
	 * thrown; otherwise, the existing author and publisher are used (changes to
	 * that data in the input are ignored).
	 *
	 * @param bookId the ID number of the book to update
	 * @param input  the book data to update.
	 * @return the updated book
	 * @throws TransactionException if the book is not found, or on internal error.
	 */
	@PutMapping({"/book/{bookId}", "/book/{bookId}/"})
	public Book updateBook(@PathVariable("bookId") final int bookId,
			@RequestBody final Book input) throws TransactionException {
		final Book book = service.getBook(bookId);
		if (book == null) {
			throw new RetrieveException("Book not found");
		} else {
			final Author author = input.getAuthor();
			if (author != null) {
				final Author dbAuthor = service.getAuthor(author.getId());
				if (dbAuthor == null) {
					throw new RetrieveException("Author not found");
				} else {
					book.setAuthor(dbAuthor);
				}
			}
			final Publisher publisher = input.getPublisher();
			if (publisher != null) {
				final Publisher dbPublisher = service.getPublisher(publisher.getId());
				if (dbPublisher == null) {
					throw new RetrieveException("Publisher not found");
				} else {
					book.setPublisher(dbPublisher);
				}
			}
			book.setTitle(input.getTitle());
			service.updateBook(book);
			return service.getBook(bookId);
		}
	}

	/**
	 * Create an author with the given name.
	 * @param body the request body, which must contain a 'name' field.
	 * @return the created author
	 * @throws TransactionException on internal error
	 */
	@PostMapping({ "/author", "/author/" })
	public ResponseEntity<Author> createAuthor(
			@RequestBody final Map<String, String> body)
			throws TransactionException {
		if (body.containsKey("name")) {
			return new ResponseEntity(service.createAuthor(body.get("name")), HttpStatus.CREATED);
		} else {
			return new ResponseEntity(HttpStatus.BAD_REQUEST); // TODO: explain what field is missing
		}
	}
	/**
	 * Create a publisher with the specified parameters.
	 * @param body the request body, which must contain a 'name' field;
	 *             'address' and 'phone' fields are also recognized.
	 * @throws TransactionException on internal error
	 */
	@PostMapping({"/publisher", "/publisher/"})
	public ResponseEntity<Publisher> createPublisher(
			@RequestBody final Map<String, String> body)
			throws TransactionException {
		if (body.containsKey("name")) {
			return new ResponseEntity(service.createPublisher(body.get("name"),
						body.getOrDefault("address", ""),
						body.getOrDefault("phone", "")), HttpStatus.CREATED);
		} else {
			return new ResponseEntity(HttpStatus.BAD_REQUEST); // TODO: explain what field is missing
		}
	}

	/**
	 * Create a book with the specified parameters.
	 * @param body the request body. It must have a title, its ID is
	 *             ignored, and its author and publisher state other than
	 *             ID are ignored unless none with the specified IDs exist,
	 *             in which case they are added to the database and the
	 *             provided IDs are ignored.
	 * @param author the author to assign the book to
	 * @param publisher the publisher to assign the book to
	 */
	@PostMapping({"/book", "/book/"})
	public ResponseEntity<Book> createBook(@RequestBody final Book body)
			throws TransactionException {
		final String title = body.getTitle();
		if (title == null) {
			return new ResponseEntity(HttpStatus.BAD_REQUEST); // TODO: explain what field is missing
		}
		Author actualAuthor;
		if (body.getAuthor() == null) {
			actualAuthor = null;
		} else {
			final Author dbAuthor = service.getAuthor(body.getAuthor().getId());
			if (dbAuthor == null) {
				actualAuthor = service.createAuthor(body.getAuthor().getName());
			} else {
				actualAuthor = dbAuthor;
			}
		}
		Publisher actualPublisher;
		if (body.getPublisher() == null) {
			actualPublisher = null;
		} else {
			final Publisher dbPublisher = service.getPublisher(body.getPublisher().getId());
			if (dbPublisher == null) {
				actualPublisher = service.createPublisher(
						body.getPublisher().getName(),
						body.getPublisher().getAddress(),
						body.getPublisher().getPhone());
			} else {
				actualPublisher = dbPublisher;
			}
		}
		return new ResponseEntity(
				service.createBook(body.getTitle(), actualAuthor,
					actualPublisher),
				HttpStatus.CREATED);
	}

	/**
	 * Delete the author with the given ID.
	 * @param authorId the ID of the author to delete.
	 * @throws TransactionException on internal error
	 */
	@DeleteMapping({"/author/{authorId}", "/author/{authorId}/"})
	public void deleteAuthor(@PathVariable("authorId") final int authorId) throws TransactionException {
		final Author author = service.getAuthor(authorId);
		if (author != null) {
			service.deleteAuthor(author);
		}
	}

	/**
	 * Delete the publisher with the given ID.
	 * @param publisherId the ID of the publisher to delete
	 * @throws TransactionException on internal error
	 */
	@DeleteMapping({ "/publisher/{publisherId}", "/publisher/{publisherId}/" })
	public void deletePublisher(@PathVariable("publisherId") final int publisherId)
			throws TransactionException {
		final Publisher publisher = service.getPublisher(publisherId);
		if (publisher != null) {
			service.deletePublisher(publisher);
		}
	}

	/**
	 * Delete the book with the given ID.
	 * @param bookId the ID of the book to delete
	 * @throws TransactionException on internal error
	 */
	@DeleteMapping({ "/book/{bookId}", "/book/{bookId}/" })
	public void deleteBook(@PathVariable("bookId") final int bookId)
			throws TransactionException {
		final Book book = service.getBook(bookId);
		if (book != null) {
			service.deleteBook(book);
		}
	}
}
