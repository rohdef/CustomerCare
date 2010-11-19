package as.markon.client;

import com.google.gwt.core.client.GWT;

import as.markon.viewmodel.Salesman;

public class Global {
	private DataServiceAsync dataService = GWT.create(DataService.class);
	private Salesman salesman;
	
	private static Global instance = new Global();
	public static Global getInstance() {
		return instance;
	}
	
	private Global() {
	}
	
	public DataServiceAsync getDataService() {
		return dataService;
	}
	
	public Salesman getCurrentSalesman() {
		return salesman;
	}
	
	public void setCurrentSalesman(Salesman salesman) {
		this.salesman = salesman;
	}
}
