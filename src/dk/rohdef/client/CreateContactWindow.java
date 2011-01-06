package dk.rohdef.client;

import java.util.ArrayList;


import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

import dk.rohdef.client.events.ContactEvent;
import dk.rohdef.client.events.ContactListener;
import dk.rohdef.client.panels.CreateContactPanel;
import dk.rohdef.client.services.Global;
import dk.rohdef.viewmodel.Contact;

public class CreateContactWindow extends Window {
	private ArrayList<ContactListener> contactListeners;
	
	public CreateContactWindow(final int salesmanid, final int companyid) {
		this.setHeading("Opret ny kontakt");
		this.setLayout(new RowLayout(Orientation.VERTICAL));
		this.setWidth(350);

		contactListeners = new ArrayList<ContactListener>();
		CreateContactPanel createContact = new CreateContactPanel();
		createContact.setWidth("100%");
		
		createContact.addNewContactListener(new ContactListener() {
			public void handleEvent(final ContactEvent be) {
				Global.getInstance().getDataService()
					.insertContact(be.getContact(), salesmanid, companyid,
							new AsyncCallback<Integer>() {
								public void onSuccess(Integer result) {
									Contact contact = be.getContact();
									contact.set("contactid", result);
				
									fireContactEvent(new ContactEvent(
											ContactEvent.NEW_CONTACT_TYPE,
											contact));
									
									hide();
								}
								
								public void onFailure(Throwable caught) {
								}
							});
			}
		});
		
		this.add(createContact);
	}

	public void addContactListener(ContactListener l) {
		contactListeners.add(l);
	}
	
	public void removeContactListener(ContactListener l) {
		contactListeners.remove(l);
	}
	
	private void fireContactEvent(ContactEvent event) {
		for (ContactListener l : contactListeners)
			l.handleEvent(event);
	}
}
