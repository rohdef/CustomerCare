package as.markon.client;

import java.util.ArrayList;
import java.util.List;

import as.markon.viewmodel.City;
import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;
import as.markon.viewmodel.Importance;
import as.markon.viewmodel.Salesman;
import as.markon.viewmodel.Trade;

import com.google.gwt.user.client.rpc.AsyncCallback;

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
	void sendTrade(Trade t, AsyncCallback<Void> callback);
	void sendImportance(Importance i, AsyncCallback<Void> callback);
	void createCompany(Company company, ArrayList<Contact> contacts, Salesman salesman, AsyncCallback<Integer> callback);
	void updateCompany(Company company, AsyncCallback<Void> callback);
	void deleteCompanies(List<Company> companies,
			AsyncCallback<Void> callback);
	void deleteCompany(Company company, AsyncCallback<Void> callback);
}
