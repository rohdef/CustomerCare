package dk.rohdef.client;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionEvent;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

import dk.rohdef.viewmodel.Salesman;

public class CustomerCare implements EntryPoint {
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		final Login l = new Login();
		
		l.addListener(Events.Select, new Listener<SelectionEvent<Salesman>>() {
			public void handleEvent(SelectionEvent<Salesman> be) {
				RootPanel.get().remove(l);
				RootPanel.get().add(new CustomerView(be.getModel()));
				dk.rohdef.client.services.Global.getInstance().getDataService().loaded(
						new AsyncCallback<Void>() {
							public void onSuccess(Void result) {
							}
							
							public void onFailure(Throwable caught) {
							}
						});
			}
		});
		RootPanel.get().add(l);
	}
}
