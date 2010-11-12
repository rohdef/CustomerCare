package as.markon.client;

import java.util.ArrayList;

import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DataServiceAsync {
	void getCompanies(int salesmanId, AsyncCallback<ArrayList<Company>> callback);
	void close(AsyncCallback<Void> callback);
	void getContactsFor(Company company,
			AsyncCallback<ArrayList<Contact>> callback);
}
