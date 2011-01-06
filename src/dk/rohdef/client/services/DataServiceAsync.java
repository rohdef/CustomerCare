package dk.rohdef.client.services;

import java.util.ArrayList;
import java.util.List;


import com.google.gwt.user.client.rpc.AsyncCallback;

import dk.rohdef.viewmodel.City;
import dk.rohdef.viewmodel.Company;
import dk.rohdef.viewmodel.Contact;
import dk.rohdef.viewmodel.Importance;
import dk.rohdef.viewmodel.LabelRecipient;
import dk.rohdef.viewmodel.Salesman;
import dk.rohdef.viewmodel.Trade;

public interface DataServiceAsync {
	void getCompanies(Salesman salesman, AsyncCallback<ArrayList<Company>> callback);
	void close(AsyncCallback<Void> callback);
	void getContactsFor(Salesman salesman, Company company,	AsyncCallback<ArrayList<Contact>> callback);
	void getTrades(AsyncCallback<ArrayList<Trade>> callback);
	void getImportance(String name, AsyncCallback<Importance> callback);
	void getCities(AsyncCallback<ArrayList<City>> callback);
	void sendMail(String user, String subject, String message,
			List<String> recipients, AsyncCallback<Void> callback);
	void getSalesmen(AsyncCallback<ArrayList<Salesman>> callback);
	void sendImportance(Importance i, AsyncCallback<Void> callback);
	void createCompany(Company company, ArrayList<Contact> contacts, Salesman salesman, AsyncCallback<Integer> callback);
	void updateCompany(Company company, AsyncCallback<Void> callback);
	void deleteCompanies(List<Company> companies,
			AsyncCallback<Void> callback);
	void deleteCompany(Company company, AsyncCallback<Void> callback);
	void deleteContacts(List<Contact> contacts, AsyncCallback<Void> callback);
	void deleteContact(Contact contact, AsyncCallback<Void> callback);
	void insertContact(Contact contact, int salesmanid, int companyid,
			AsyncCallback<Integer> callback);
	void updateContact(Contact contact, AsyncCallback<Void> callback);
	void getProspectCompanies(AsyncCallback<ArrayList<Company>> callback);
	void addTrade(Trade trade, AsyncCallback<Void> callback);
	void deleteTrade(Trade trade, AsyncCallback<Void> callback);
	void deleteSalesman(Salesman salesman, AsyncCallback<Void> callback);
	void updateSalesman(Salesman salesman, AsyncCallback<Void> callback);
	void insertSalesman(Salesman salesman, AsyncCallback<Integer> callback);
	void createPdf(ArrayList<LabelRecipient> recipients,
			AsyncCallback<Integer> callback);
	void getAppCompany(AsyncCallback<Company> callback);
	void getAllContacts(Company company,
			AsyncCallback<ArrayList<Contact>> callback);
	void getCompanyFor(Contact contact, AsyncCallback<Company> callback);
	void getAllCompanies(AsyncCallback<ArrayList<Company>> callback);
}
