package dk.rohdef.client.i18n;

import com.google.gwt.i18n.client.Messages;;

public interface CustomerCareI18n extends Messages {
	@DefaultMessage("{0} - CustomerCare")
	public String pageTitle(String companyName);
	
	@DefaultMessage("Customers")
	public String customers();
	
	@DefaultMessage("Change login")
	public String changeLogin();
	
	@DefaultMessage("Which salesman do you wist to see the customer listing for?")
	public String whichSalesman();
	
	@DefaultMessage("Create event")
	public String createEvent();
	
	@DefaultMessage("Send e-mail")
	public String sendEMail();
	
	@DefaultMessage("Insert search string")
	public String insertSearchStringHere();
	

	@DefaultMessage("Search results")
	public String searchResults();
	
	@DefaultMessage("Save")
	public String save();
	
	@DefaultMessage("Previous")
	public String previous();
	
	@DefaultMessage("Next")
	public String next();
	
	@DefaultMessage("Search")
	public String search();
	
	@DefaultMessage("Mangage trades")
	public String manageTrades();
	
	//
	// SALESPEOPLE
	//
	@DefaultMessage("Manages salespeople")
	public String manageSalespeople();

	@DefaultMessage("Move to salesman")
	public String moveToSalesman();
	
	@DefaultMessage("Which salesperson do you want to transfer the customer to?")
	public String whichSalesPersonToMoveTo();
	
	@DefaultMessage("Chose salesman")
	public String choseSalesman();
	
	@DefaultMessage("Select salesman")
	public String selectSalesman();
	
	
	
	@DefaultMessage("Are you sure you want to delete?")
	public String deleteCompanyDialogTitle();
	
	@DefaultMessage("Warning! This cannot be undone!")
	public String cannotBeUndone();
	
	@DefaultMessage("Are you sure you want to delete the selected company?")
	public String doYouWantToDelteTheCompany();
	
	@DefaultMessage("Delete company")
	public String deleteCompany();
	
	@DefaultMessage("Cancel")
	public String cancel();
	
	@DefaultMessage("There is no companies in the list.")
	public String emptyCompanyList();
	
	@DefaultMessage("Do you wish to create a prospect company?")
	public String createProspectTitle();
	
	@DefaultMessage("Do you want to create a company without contacts?")
	public String noContactsQuestion();
	
	@DefaultMessage("A company without any contacts will be a prospect.")
	public String noContactCompanyIsProspect();
	
	@DefaultMessage("Create prospect")
	public String createProspect();
	
	
	//
	// CONTACTS
	//
	
	@DefaultMessage("Contacts")
	public String contacts();
	
	@DefaultMessage("Contact list")
	public String contactList();
	
	@DefaultMessage("Name")
	public String contactName();
	
	@DefaultMessage("Title")
	public String contactTitle();
	
	@DefaultMessage("Phone")
	public String contactPhone();
	
	@DefaultMessage("E-mail")
	public String contactEmail();
	
	@DefaultMessage("No contacts has been created yet")
	public String noContacts();
	
	@DefaultMessage("Accepts e-mail")
	public String contactAcceptsMail();
	
	@DefaultMessage("Comments")
	public String contactComments();
	
	@DefaultMessage("Add contact")
	public String addContact();
	
	@DefaultMessage("Delete contact")
	public String deleteContact();
	
	@DefaultMessage("Are you sure you want to delete {0}?")
	public String areYouSureYouWantToDeleteContact(String contactName);
	
	@DefaultMessage("Do you want to delete {0}?")
	public String doYouWantToDeleteContact(String contactName);
	
	@DefaultMessage("Delete the contact")
	public String deleteTheContact();
	
	
	//
	// COMPANIES
	//	
	@DefaultMessage("Insert company details")
	public String insertCompanyDetails();
	
	@DefaultMessage("Company")
	public String company();
	
	@DefaultMessage("Companies")
	public String companies();
	
	@DefaultMessage("Customer list")
	public String customerList();
	
	@DefaultMessage("Prospects")
	public String prospects();
	
	@DefaultMessage("All companies")
	public String allCompanies();
	
	@DefaultMessage("Company data")
	public String companyData();
	
	@DefaultMessage("Company name")
	public String companyName();
	
	@DefaultMessage("Address")
	public String address();
	
	@DefaultMessage("Postal")
	public String postal();
	
	@DefaultMessage("City")
	public String city();
	
	@DefaultMessage("Phone")
	public String phone();
	
	@DefaultMessage("Trade")
	public String trade();
	
	@DefaultMessage("No trade selected")
	public String noTradeSelected();
	
	@DefaultMessage("Group")
	public String group();
	
	@DefaultMessage("Comments")
	public String comments();
	
	@DefaultMessage("Create new company")
	public String createCompany();
	
	@DefaultMessage("Delete selected companies")
	public String deleteSelectedCompanies();
}
