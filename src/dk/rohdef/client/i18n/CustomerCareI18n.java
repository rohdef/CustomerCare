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
	
	@DefaultMessage("Insert search string")
	public String insertSearchStringHere();
	
	@DefaultMessage("Save")
	public String save();
	
	@DefaultMessage("Previous")
	public String previous();
	
	@DefaultMessage("Next")
	public String next();
	
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
	
	@DefaultMessage("Search")
	public String search();
	
	@DefaultMessage("Create new company")
	public String createCompany();
	
	@DefaultMessage("Delete selected companies")
	public String deleteSelectedCompanies();
	
	@DefaultMessage("Mangage trades")
	public String manageTrades();
	
	@DefaultMessage("Manages salespeople")
	public String manageSalespeople();
	
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
	
	@DefaultMessage("Search results")
	public String searchResults();
	
	
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
}
