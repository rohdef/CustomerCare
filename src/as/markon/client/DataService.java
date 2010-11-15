package as.markon.client;

import java.util.ArrayList;

import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;
import as.markon.viewmodel.Trade;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("data")
public interface DataService extends RemoteService {
	public ArrayList<Company> getCompanies(int salesmanId);
	public void close();
	public ArrayList<Contact> getContactsFor(Company company);
	public ArrayList<Trade> getTrades();
}
