package dk.rohdef.client.i18n;

import com.google.gwt.i18n.client.Messages;;

public interface CustomerCareI18n extends Messages {
	@DefaultMessage("{0} - CustomerCare")
	public String pageTitle(String companyName);
	
	@DefaultMessage("Insert search string")
	public String insertSearchStringHere();
	
	@DefaultMessage("Contact list")
	public String contactList();
	
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
	
}
