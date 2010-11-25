package as.markon.client;

import java.util.ArrayList;
import java.util.List;

import as.markon.viewmodel.City;
import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;
import as.markon.viewmodel.Importance;
import as.markon.viewmodel.Salesman;
import as.markon.viewmodel.Trade;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("data")
public interface DataService extends RemoteService {
	public void close();
	public ArrayList<Trade> getTrades();
	public ArrayList<City> getCities();
	public ArrayList<Salesman> getSalesmen();
	
	// Companies
	public ArrayList<Company> getCompanies(Salesman salesman);
	public Integer createCompany(Company company, ArrayList<Contact> contacts, Salesman salesman);
	public void updateCompany(Company company);
	public void deleteCompanies(List<Company> companies);
	public void deleteCompany(Company company);

	// Contacts
	public ArrayList<Contact> getContactsFor(Salesman salesman, Company company);
	public void deleteContacts(List<Contact> contacts);
	public void deleteContact(Contact contact);
	public void insertContact(Contact contact, int salesmanid, int companyid);
	public void updateContact(Contact contact);
	
	// This logically belong elsewhere, but it wouldn't work :(
	public void sendMail(String user, String subject, String message, List<String> recipients);
	
	// Gah, there should be a better way to register types for serialization
	public Importance getImportance(String name);
	public void sendImportance(Importance i);
	public void sendTrade(Trade t);
}
