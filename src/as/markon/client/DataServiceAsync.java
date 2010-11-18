package as.markon.client;

import java.util.ArrayList;
import java.util.List;

import as.markon.viewmodel.City;
import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;
import as.markon.viewmodel.Importance;
import as.markon.viewmodel.Trade;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DataServiceAsync {
	void getCompanies(int salesmanId, AsyncCallback<ArrayList<Company>> callback);
	void close(AsyncCallback<Void> callback);
	void getContactsFor(Company company,
			AsyncCallback<ArrayList<Contact>> callback);
	void getTrades(AsyncCallback<ArrayList<Trade>> callback);
	void getImportance(String name, AsyncCallback<Importance> callback);
	void getCities(AsyncCallback<ArrayList<City>> callback);
	void sendMail(String user, String password, String subject, String message,
			List<String> recipients, AsyncCallback<Void> callback);
}
