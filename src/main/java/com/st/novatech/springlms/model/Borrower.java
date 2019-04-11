package com.st.novatech.springlms.model;

import java.util.Objects;

/**
 * A user of the library who is able to check out books.
 *
 * @author Salem Ozaki
 * @author Jonathan Lovelace
 */
public class Borrower {
	/**
	 * The borrower's card number, used as this object's identity.
	 */
	private final int cardNo;
	/**
	 * The borrower's name.
	 */
	private String name;
	/**
	 * The borrower's address.
	 */
	private String address;
	/**
	 * The borrower's phone number.
	 */
	private String phone;

	/**
	 * To construct a Borrower object callers must supply the card number, name,
	 * address, and phone number.
	 *
	 * @param cardNo  the borrower's card number
	 * @param name    the borrower's name, which should not be null.
	 * @param address the borrower's address, which should not be null.
	 * @param phone   the borrower's phone number, as a string, which should not be
	 *                null.
	 */
	public Borrower(final int cardNo, final String name, final String address,
			final String phone) {
		this.cardNo = cardNo;
		this.name = name;
		this.address = address;
		this.phone = phone;
	}

	/**
	 * Get the borrower's name, which will not be null.
	 *
	 * @return the borrower's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Change the borrower's name.
	 *
	 * @param name the borrower's new name, which should not be null.
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Get the borrower's address, which will not be null.
	 *
	 * @return the borrower's address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Change the borrower's address.
	 *
	 * @param address the borrower's new address, which should not be null.
	 */
	public void setAddress(final String address) {
		this.address = address;
	}

	/**
	 * Get the borrower's phone number as a non-null string.
	 *
	 * @return the borrower's phone number.
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * Change the borrower's phone number.
	 *
	 * @param phone the borrower's new phone number, which should not be null.
	 */
	public void setPhone(final String phone) {
		this.phone = phone;
	}

	/**
	 * Get the borrower's card number.
	 *
	 * @return the borrower's card number
	 */
	public int getCardNo() {
		return cardNo;
	}

	/**
	 * We use only the ID for this object's hash-code.
	 */
	@Override
	public int hashCode() {
		return cardNo;
	}

	/**
	 * An object is equal to this one iff it is a Borrower with the same card
	 * number, name, address, and phone number.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof Borrower) {
			return cardNo == ((Borrower) obj).getCardNo()
					&& Objects.equals(name, ((Borrower) obj).getName())
					&& Objects.equals(address, ((Borrower) obj).getAddress())
					&& Objects.equals(phone, ((Borrower) obj).getPhone());
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "Borrower " + name + "(" + cardNo + ") at " + address + " with phone: " + phone;
	}
}
