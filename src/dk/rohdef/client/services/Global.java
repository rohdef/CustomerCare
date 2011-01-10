package dk.rohdef.client.services;

import com.google.gwt.core.client.GWT;

import dk.rohdef.client.i18n.CustomerCareI18n;
import dk.rohdef.viewmodel.Salesman;


public class Global {
	private DataServiceAsync dataService = GWT.create(DataService.class);
	private CustomerCareI18n i18n = GWT.create(CustomerCareI18n.class);
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
	
	public CustomerCareI18n getI18n() {
		return i18n;
	}
	
	public Salesman getCurrentSalesman() {
		return salesman;
	}
	
	public void setCurrentSalesman(Salesman salesman) {
		this.salesman = salesman;
	}
}
