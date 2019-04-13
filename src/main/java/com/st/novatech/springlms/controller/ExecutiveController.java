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
import com.st.novatech.springlms.model.Borrower;
import com.st.novatech.springlms.model.Branch;
import com.st.novatech.springlms.service.AdministratorService;

/**
 * Controller for administrators with the power to manage branch and borrower
 * details and override due dates.
 *
 * <p>FIXME: Limit access to most of these endpoints to authorized users
 *
 * <p>FIXME: Commit after making changes to the database.
 * @author Jonathan Lovelace
 */
@RestController
public final class ExecutiveController {
	/**
	 * Service class used to handle requests.
	 */
	@Autowired
	private AdministratorService service;
	/**
	 * Get all branches from the database. Spring turns the list into JSON (or XML?).
	 * @return the list of all branches in the database.
	 * @throws TransactionException on internal error.
	 */
	@RequestMapping({"/branches", "/branches/"})
	public List<Branch> getBranches() throws TransactionException {
		return service.getAllBranches();
	}
	/**
	 * Get all borrowers from the database. Spring turns the list into JSON (or XML?).
	 * @return the list of all borrowers in the database
	 * @throws TransactionException on internal error
	 */
	@RequestMapping({"/borrowers", "/borrowers/"})
	public List<Borrower> getBorrowers() throws TransactionException {
		return service.getAllBorrowers();
	}
	/**
	 * Get a branch by its ID number.
	 * @param branchId the ID number of the branch
	 * @return the branch with that ID
	 * @throws TransactionException if branch not found, or on internal error
	 */
	@RequestMapping({"/branch/{branchId}", "/branch/{branchId}/"})
	public Branch getBranch(@PathVariable("branchId") final int branchId)
			throws TransactionException {
		final Branch branch = service.getbranch(branchId);
		if (branch == null) {
			throw new RetrieveException("Branch not found");
		} else {
			return branch;
		}
	}
	/**
	 * Get a borrower by his or her card number.
	 * @param cardNumber the borrower's card number
	 * @return the borrower with that card number
	 * @throws TransactionException if borrower not found, or on internal error
	 */
	@RequestMapping({"/borrower/{cardNumber}", "/borrower/{cardNumber}/"})
	public Borrower getBorrower(@PathVariable("cardNumber") final int cardNumber)
			throws TransactionException {
		final Borrower borrower = service.getBorrower(cardNumber);
		if (borrower == null) {
			throw new RetrieveException("Borrower not found");
		} else {
			return borrower;
		}
	}
	/**
	 * Update a branch by its ID number.
	 * @param branchId the ID number of the branch to update
	 * @param input the branch data to update
	 * @return the updated branch
	 * @throws TransactionException if author not found or on internal error
	 */
	@RequestMapping(path = { "/branch/{branchId}",
			"/branch/{branchId}/" }, method = RequestMethod.PUT)
	public Branch updateBranch(@PathVariable("branchId") final int branchId,
			@RequestBody final Branch input) throws TransactionException {
		final Branch branch = service.getbranch(branchId);
		if (branch == null) {
			throw new RetrieveException("Branch not found");
		} else {
			branch.setName(input.getName());
			branch.setAddress(input.getAddress());
			service.updateBranch(branch);
			return service.getbranch(branchId);
		}
	}
	/**
	 * Update a borrower by his or her card number.
	 * @param cardNumber the card number of the borrower to update
	 * @param input the borrower details to update
	 * @return the updated borrower
	 * @throws TransactionException if borrower not found or on internal error
	 */
	@RequestMapping(path = { "/borrower/{cardNumber}",
			"/borrower/{cardNumber}/" }, method = RequestMethod.PUT)
	public Borrower updateBorrower(@PathVariable("cardNumber") final int cardNumber,
			@RequestBody final Borrower input) throws TransactionException {
		final Borrower borrower = service.getBorrower(cardNumber);
		if (borrower == null) {
			throw new RetrieveException("Borrower not found");
		} else {
			borrower.setName(input.getName());
			borrower.setAddress(input.getAddress());
			borrower.setPhone(input.getPhone());
			service.updateBorrower(borrower);
			return service.getBorrower(cardNumber);
		}
	}
	/**
	 * Create a branch with the given name and address.
	 * @param name the name of the branch
	 * @param address the address of the branch
	 * @return the created branch
	 * @throws TransactionException on internal error
	 */
	@RequestMapping(path = {"/branch", "/branch/"}, method = RequestMethod.POST)
	public Branch createBranch(@RequestParam("name") final String name,
			@RequestParam(name = "address", defaultValue = "") final String address)
			throws TransactionException {
		return service.createBranch(name, address);
	}
	/**
	 * Create a borrower record with the given name, address, and phone data.
	 * @param name the name of the borrower
	 * @param address the address of the borrower
	 * @param phone the phone number of the borrower
	 * @return the created borrower record
	 * @throws TransactionException on internal error
	 */
	@RequestMapping(path = {"/borrower", "/borrower/"}, method = RequestMethod.POST)
	public Borrower createBorrower(@RequestParam("name") final String name,
			@RequestParam(name = "address", defaultValue = "") final String address,
			@RequestParam(name = "phone", defaultValue = "") final String phone)
			throws TransactionException {
		return service.createBorrower(name, address, phone);
	}
	/**
	 * Delete the branch with the given ID.
	 * @param branchId the ID of the branch to delete.
	 * @throws TransactionException on internal error
	 */
	@RequestMapping(path = {"/branch/{branchId}", "/branch/{branchId}/"}, method = RequestMethod.DELETE)
	public void deleteBranch(@PathVariable("branchId") final int branchId) throws TransactionException {
		final Branch branch = service.getbranch(branchId);
		if (branch != null) {
			service.deleteBranch(branch);
		}
	}
	/**
	 * Delete the borrower with the given card number.
	 * @param cardNumber the card number of the borrower to delete from the database
	 * @throws TransactionException on internal error
	 */
	@RequestMapping(path = { "/borrower/{cardNumber}",
			"/borrower/{cardNumber}/" }, method = RequestMethod.DELETE)
	public void deleteBorrower(@PathVariable("cardNumber") final int cardNumber)
			throws TransactionException {
		final Borrower borrower = service.getBorrower(cardNumber);
		if (borrower != null) {
			service.deleteBorrower(borrower);
		}
	}
}
