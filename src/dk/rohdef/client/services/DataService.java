package dk.rohdef.client.services;

import java.util.ArrayList;
import java.util.List;


import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import dk.rohdef.viewmodel.City;
import dk.rohdef.viewmodel.Company;
import dk.rohdef.viewmodel.Contact;
import dk.rohdef.viewmodel.Importance;
import dk.rohdef.viewmodel.LabelRecipient;
import dk.rohdef.viewmodel.Salesman;
import dk.rohdef.viewmodel.Trade;

@RemoteServiceRelativePath("data")
public interface DataService extends RemoteService {
	public void close();

	public ArrayList<City> getCities();
	
	// Companies
	public ArrayList<Company> getCompanies(Salesman salesman);
	public ArrayList<Company> getProspectCompanies();
	public ArrayList<Company> getAllCompanies();
	public Integer createCompany(Company company, ArrayList<Contact> contacts,
			Salesman salesman);
	public void updateCompany(Company company);
	public void deleteCompanies(List<Company> companies);
	public void deleteCompany(Company company);
	/**
	 * Get the application company, this is the company that hosts the application. This
	 * is used for when creating events and setting the signature in mails.
	 * @return
	 */
	public Company getAppCompany();
	/**
	 * 
	 * @param contact
	 * @return the company associated with the contact
	 */
	public Company getCompanyFor(Contact contact);

	// Contacts
	public ArrayList<Contact> getContactsFor(Salesman salesman, Company company);
	public ArrayList<Contact> getAllContacts(Company company);
	public void deleteContacts(List<Contact> contacts);
	public void deleteContact(Contact contact);
	/**
	 * Create a new contact and get the new id back
	 * @param contact
	 * @param salesmanid
	 * @param companyid
	 * @return the id of the new contact
	 */
	public int insertContact(Contact contact, int salesmanid, int companyid);
	public void updateContact(Contact contact);
	
	// Salespeople
	/**
	 * Returns the list of existing salesmen
	 */
	public ArrayList<Salesman> getSalesmen();
	/**
	 * Adds a new salesman to the dataservice
	 * @param salesman to add to the dataservice
	 * @return the unique id of the new salesman (remember to set it on the salesman
	 * if you need to use it for other operations).
	 */
	public int insertSalesman(Salesman salesman);
	/**
	 * Updates the salesman in the dataservice with the current values. Be aware
	 * that this will fail if you have modified the id.
	 * @param salesman
	 */
	public void updateSalesman(Salesman salesman);
	/**
	 * Deletes the salesman from the dataservice, this cannot be undone.
	 * 
	 * The contacts from the salesman will become prospects.
	 * @param salesman
	 */
	public void deleteSalesman(Salesman salesman);
	
	// Trades
	public ArrayList<Trade> getTrades();
	public void addTrade(Trade trade);
	public void deleteTrade(Trade trade);
	
	// This logically belong elsewhere, but it wouldn't work :(
	public void sendMail(String user, String subject, String message, List<String> recipients);
	public Integer createPdf(ArrayList<LabelRecipient> recipients);
	
	// Gah, there should be a better way to register types for serialization
	public Importance getImportance(String name);
	public void sendImportance(Importance i);
}
