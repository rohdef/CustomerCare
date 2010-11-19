package as.markon.client;


import java.util.ArrayList;

import as.markon.viewmodel.Salesman;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.Observable;
import com.extjs.gxt.ui.client.event.SelectionEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.custom.Portal;
import com.extjs.gxt.ui.client.widget.custom.Portlet;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class Login extends LayoutContainer implements Observable {
	public Login() {
	}

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		ContentPanel panel = new ContentPanel();
		panel.setBodyStyle("padding: 0.5em;");
		panel.setWidth("80%");
		panel.setBorders(true);
		panel.setHeading("Hvis kundekartotek vil du se?");

		
		final Portal content = new Portal(3);
		content.setColumnWidth(0, .33);
		content.setColumnWidth(1, .33);
		content.setColumnWidth(2, .33);
//		content.setWidth("80%");

		final Listener<SelectionEvent<Salesman>> selectSalesman = new Listener<SelectionEvent<Salesman>>() {
			public void handleEvent(SelectionEvent<Salesman> be) {
				fireEvent(Events.Select, be);
			}
		};
		
		DataServiceAsync dataService = Global.getInstance().getDataService();
		dataService.getSalesmen(new AsyncCallback<ArrayList<Salesman>>() {
			public void onSuccess(ArrayList<Salesman> result) {
				int count = 0;
				for (Salesman s : result) {
					LoginItem loginItem = new LoginItem(s);
					loginItem.addListener(Events.Select, selectSalesman);
					content.add(loginItem, (count++%3));
				}
			}
			
			public void onFailure(Throwable caught) {
			}
		});
		
		panel.add(content);
		
		this.add(panel);
	}
	
	private class LoginItem extends Portlet {
		private Salesman salesman;
		
		public LoginItem(final Salesman s) {
			this.salesman = s;
			this.setHeading(s.getSalesman());
			
			Button b = new Button();
			b.setText(s.getSalesman());
			b.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					fireEvent(Events.Select, new SelectionEvent<Salesman>(this, salesman));
				}
			});
			
			this.add(b);
		}
	}
}
