package dk.rohdef.client;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionEvent;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import dk.rohdef.client.i18n.CustomerCareI18n;
import dk.rohdef.client.services.DataServiceAsync;
import dk.rohdef.viewmodel.Company;
import dk.rohdef.viewmodel.Salesman;

public class CustomerCare implements EntryPoint {
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		final Login l = new Login();
		final DataServiceAsync dataservice =
			dk.rohdef.client.services.Global.getInstance().getDataService();
		final CustomerCareI18n i18n = GWT.create(CustomerCareI18n.class);
		
		l.addListener(Events.Select, new Listener<SelectionEvent<Salesman>>() {
			public void handleEvent(SelectionEvent<Salesman> be) {
				RootPanel.get().remove(l);
				RootPanel.get().add(new CustomerView(be.getModel()));
				dataservice.loaded(
						new AsyncCallback<Void>() {
							public void onSuccess(Void result) {
							}
							
							public void onFailure(Throwable caught) {
							}
						});
			}
		});
		
		dataservice.getAppCompany(new AsyncCallback<Company>() {
			public void onSuccess(Company result) {
				Window.setTitle(i18n.pageTitle(result.getCompanyName()));
			}
			
			public void onFailure(Throwable caught) {
			}
		});
		
		RootPanel.get().add(l);
	}
}
