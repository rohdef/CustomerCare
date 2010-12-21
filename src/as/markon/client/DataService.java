package as.markon.client;

import java.util.ArrayList;
import java.util.List;

import as.markon.viewmodel.City;
import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;
import as.markon.viewmodel.Importance;
import as.markon.viewmodel.LabelRecipient;
import as.markon.viewmodel.Salesman;
import as.markon.viewmodel.Trade;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("data")
public interface DataService extends RemoteService {
	public void close();

	public ArrayList<City> getCities();
	
	// Companies
	public ArrayList<Company> getCompanies(Salesman salesman);
	public ArrayList<Company> getProspectCompanies();
	public Integer createCompany(Company company, ArrayList<Contact> contacts, Salesman salesman);
	public void updateCompany(Company company);
	public void deleteCompanies(List<Company> companies);
	public void deleteCompany(Company company);
	public Company getAppCompany();

	// Contacts
	public ArrayList<Contact> getContactsFor(Salesman salesman, Company company);
	public ArrayList<Contact> getAllContacts(Company company);
	public void deleteContacts(List<Contact> contacts);
	public void deleteContact(Contact contact);
	public void insertContact(Contact contact, int salesmanid, int companyid);
	public void updateContact(Contact contact);
	
	// Salespeople
	public ArrayList<Salesman> getSalesmen();
	public int insertSalesman(Salesman salesman);
	public void updateSalesman(Salesman salesman);
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
