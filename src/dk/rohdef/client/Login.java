package dk.rohdef.client;

import java.util.ArrayList;


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
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;

import dk.rohdef.client.i18n.CustomerCareI18n;
import dk.rohdef.client.services.DataServiceAsync;
import dk.rohdef.client.services.Global;
import dk.rohdef.viewmodel.Salesman;

public class Login extends LayoutContainer implements Observable {
	CustomerCareI18n i18n;
	
	public Login() {
	}

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		i18n = Global.getInstance().getI18n();
		
		ContentPanel panel = new ContentPanel();
		panel.setBodyStyle("padding: 0.5em;");
		panel.setWidth("80%");
		panel.setBorders(true);
		panel.setHeading(i18n.whichSalesman());

		
		final Portal content = new Portal(3);
		content.setColumnWidth(0, .33);
		content.setColumnWidth(1, .33);
		content.setColumnWidth(2, .33);

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
			
			Image profileImage = new Image("http://gravatar.com/avatar/"+
					salesman.getMailMd5());
			this.add(profileImage);
			
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
