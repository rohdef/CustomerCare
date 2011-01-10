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
}
