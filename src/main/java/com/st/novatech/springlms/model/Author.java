package com.st.novatech.springlms.model;

import java.util.Objects;

/**
 * An author of books.
 *
 * @author Salem Ozaki
 * @author Jonathan Lovelace
 */
public final class Author {
	/**
	 * The author's ID in the database.
	 */
	private final int id;
	/**
	 * The author's name.
	 */
	private String name;

	/**
	 * Constructing the author object requires its ID and name.
	 *
	 * @param id   the ID used to reference the author in the database.
	 * @param name the author's name
	 */
	public Author(final int id, final String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Get the author's name, which will not be null.
	 * @return the author's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the author's name, which should not be null.
	 *
	 * @param name the author's new name.
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Get the ID used to refer to this author in the database.
	 * @return the ID used to refer to this author in the database.
	 */
	public int getId() {
		return id;
	}

	/**
	 * We use only the ID for this object's hash-code.
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * An object is equal to this one iff it is an Author with the same ID and name.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof Author) {
			return id == ((Author) obj).getId()
					&& Objects.equals(name, ((Author) obj).getName());
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "Author: " + name + "(" + id + ")";
	}
}
