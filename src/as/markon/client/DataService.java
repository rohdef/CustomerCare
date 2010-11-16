package as.markon.client;

import java.util.ArrayList;
import java.util.Set;

import as.markon.viewmodel.City;
import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;
import as.markon.viewmodel.Importance;
import as.markon.viewmodel.Trade;

import com.google.gwt.thirdparty.guava.common.collect.BiMap;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("data")
public interface DataService extends RemoteService {
	public ArrayList<Company> getCompanies(int salesmanId);
	public void close();
	public ArrayList<Trade> getTrades();
	public ArrayList<City> getCities();
	
	// Gah, there should be a better way to register types for serialization
	public ArrayList<Contact> getContactsFor(Company company);
	public Importance getImportance(String name);
}
