package dk.rohdef.client.i18n;

import com.google.gwt.i18n.client.Messages;;

public interface CustomerCareI18n extends Messages {
	@DefaultMessage("{0} - CustomerCare")
	public String pageTitle(String companyName);
	
	@DefaultMessage("Customers")
	public String customers();
	
	@DefaultMessage("Insert search string")
	public String insertSearchStringHere();

	@DefaultMessage("Search results")
	public String searchResults();
	
	@DefaultMessage("Save")
	public String save();
	
	@DefaultMessage("Cancel")
	public String cancel();
	
	@DefaultMessage("Previous")
	public String previous();
	
	@DefaultMessage("Next")
	public String next();
	
	@DefaultMessage("Select")
	public String select();
	
	@DefaultMessage("Search")
	public String search();
	
	@DefaultMessage("Warning! This cannot be undone!")
	public String cannotBeUndone();

	
	//
	// DIV FUNCTIONS/PANELS
	//
	
	@DefaultMessage("Label settings ({0} label(s) selected)")
	public String labelSettings(int labelCount);
	
	@DefaultMessage("Recipient")
	public String recipient();
	
	@DefaultMessage("Remove recipient")
	public String removeRecipient();
	
	@DefaultMessage("Select recipients")
	public String selectRecipients();
	
	@DefaultMessage("Select all")
	public String selectAll();
	
	@DefaultMessage("Select contacts")
	public String selectContacts();
	
	@DefaultMessage("Select companies")
	public String selectCompanies();
	
	@DefaultMessage("Print labels")
	public String printLabels();
	
	@DefaultMessage("Loading")
	public String loadingTitle();
	
	@DefaultMessage("Loading data from the database, please wait.")
	public String loadingPleaseWait();

	
	//
	// E-MAIL
	//
	
	@DefaultMessage("Send e-mail")
	public String sendEMail();
	
	@DefaultMessage("Write e-mail")
	public String writeEMail();
	
	@DefaultMessage("Message")
	public String eMailMessage();
	
	@DefaultMessage("Subject")
	public String eMailSubject();
	
	@DefaultMessage("Sender")
	public String eMailSender();
	
	@DefaultMessage("Mail settings")
	public String mailSettings();
	
	@DefaultMessage("Delete the mail?")
	public String eMailDeleteDialogTitle();

	@DefaultMessage("Do you want to delete what you have written?")
	public String eMailDeleteDialogMessage();
	
	@DefaultMessage("Delete the e-mail")
	public String eMailDeleteMail();
	
	@DefaultMessage("Go back to the e-mail")
	public String eMailGoBackToMail();
	
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
	
	@DefaultMessage("Change login")
	public String changeLogin();
	
	@DefaultMessage("Which salesman do you wist to see the customer listing for?")
	public String whichSalesman();
	
	@DefaultMessage("Name")
	public String salesmanName();
	
	@DefaultMessage("Title")
	public String salesmanTitle();
	
	@DefaultMessage("There is no salespeople in the list")
	public String salespeopleEmptyList();
	
	@DefaultMessage("Create new salesman")
	public String salespeopleCreateNew();
	
	@DefaultMessage("E-mail")
	public String salespeopleEMail();
	
	@DefaultMessage("Cell phone")
	public String salespeopleCellPhone();
	
	@DefaultMessage("Salesman details")
	public String salesmanDetails();
	
	@DefaultMessage("Delete salesman")
	public String salesmanDelete();
	
	@DefaultMessage("Delete {0}")
	public String salesmanDeleteSalesman(String name);
	
	@DefaultMessage("Are you sure you want to delete {0}?")
	public String salesmanAreYouSureYouWantToDelete(String name);
	
	@DefaultMessage("Do you want to delete {0}?")
	public String salesmanDeleteDialogTitle(String name);
	
	@DefaultMessage("Save changes")
	public String salesmanSaveChanges();
	

	//
	// PROSPECTS
	//
	
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
	
	@DefaultMessage("Create new contact")
	public String createNewContactTitle();
	
	
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
	
	@DefaultMessage("Group")
	public String group();
	
	@DefaultMessage("Comments")
	public String comments();
	
	@DefaultMessage("Create new company")
	public String createCompany();
	
	@DefaultMessage("Delete selected companies")
	public String deleteSelectedCompanies();
	
	@DefaultMessage("Are you sure you want to delete?")
	public String deleteCompanyDialogTitle();
	
	@DefaultMessage("Are you sure you want to delete the selected company?")
	public String doYouWantToDelteTheCompany();
	
	@DefaultMessage("Delete company")
	public String deleteCompany();
	
	@DefaultMessage("Ok")
	public String createCompanyStopEditing();
	
	
	//
	// EVENTS
	//
	
	@DefaultMessage("Create event")
	public String createEvent();
	
	@DefaultMessage("Event details")
	public String eventDetails();
	
	@DefaultMessage("Title")
	public String eventTitleLabel();
	
	@DefaultMessage("Meeting: {0} and {1}")
	public String eventTitle(String part1, String part2);
	
	@DefaultMessage("Start date")
	public String startDate();
	
	@DefaultMessage("End date")
	public String endDate();
	
	@DefaultMessage("Start time")
	public String startTime();
	
	@DefaultMessage("End time")
	public String endTime();
	
	@DefaultMessage("Location")
	public String location();
	
	@DefaultMessage("At {0}")
	public String eventLocationAt(String location);
	
	@DefaultMessage("Meeting location")
	public String meetingLocation();


	//
	// TRADES
	//
	
	@DefaultMessage("Mangage trades")
	public String manageTrades();
	
	@DefaultMessage("Trade id")
	public String tradeId();
	
	@DefaultMessage("Trade")
	public String trade();
	
	@DefaultMessage("No trade selected")
	public String noTradeSelected();
	
	@DefaultMessage("Delete trade")
	public String tradeDelete();
	
	@DefaultMessage("There is no trades in the list")
	public String tradesEmptyList();
	
	@DefaultMessage("Trade name")
	public String tradeName();
	
	@DefaultMessage("Add trade")
	public String addTrade();
	
	@DefaultMessage("Delete the trade {0}?")
	public String tradeDeleteDialogTitle(String tradename);
	
	@DefaultMessage("Are you sure want to delete the trade {0}?")
	public String tradeDeleteDialogMessage(String tradename);
	
	@DefaultMessage("Keep trade")
	public String tradeKeep();
}
