package as.markon.client;

import as.markon.viewmodel.Salesman;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionEvent;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

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
			}
		});
		
		RootPanel.get().add(l);
	}
}
