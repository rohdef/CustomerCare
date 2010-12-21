package as.markon.client;

import as.markon.client.events.ContactEvent;
import as.markon.client.events.ContactListener;
import as.markon.client.panels.CreateContactPanel;
import as.markon.client.services.Global;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CreateContactWindow extends Window {

	public CreateContactWindow(final int salesmanid, final int companyid) {
		this.setHeading("Opret ny kontakt");
		this.setLayout(new RowLayout(Orientation.VERTICAL));
		this.setWidth(350);
		
		CreateContactPanel createContact = new CreateContactPanel();
		createContact.setWidth("100%");
		
		createContact.addNewContactListener(new ContactListener() {
			public void handleEvent(ContactEvent be) {
				Global.getInstance().getDataService()
					.insertContact(be.getContact(), salesmanid, companyid,
							new AsyncCallback<Void>() {
								public void onSuccess(Void result) {
									hide();
								}
								
								public void onFailure(Throwable caught) {
								}
							});
			}
		});
		
		this.add(createContact);
	}

}
