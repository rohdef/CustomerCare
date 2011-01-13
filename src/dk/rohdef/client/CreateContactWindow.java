package dk.rohdef.client;

import java.util.ArrayList;


import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

import dk.rohdef.client.events.ContactEvent;
import dk.rohdef.client.events.ContactListener;
import dk.rohdef.client.i18n.CustomerCareI18n;
import dk.rohdef.client.panels.ContactEditPanel;
import dk.rohdef.client.services.Global;
import dk.rohdef.viewmodel.Contact;

/**
 * Create a window for creating new contacts. This fetches the {@link ContactEditPanel} 
 * adds a cancel button and "nicefies" a bit for the panel to be show in a window.
 * @see ContactEditPanel  
 * @author Rohde Fischer <rohdef@rohdef.dk>
 */
public class CreateContactWindow extends Window {
	private ArrayList<ContactListener> contactListeners;
	private CustomerCareI18n i18n;
	
	/**
	 * 
	 * @param salesmanid
	 * @param companyid
	 */
	public CreateContactWindow(final int salesmanid, final int companyid) {
		i18n = Global.getInstance().getI18n();
		
		this.setHeading(i18n.createNewContactTitle());
		this.setBorders(false);
		this.setIcon(IconHelper.createPath("images/user_add.gif"));
		this.setLayout(new RowLayout(Orientation.VERTICAL));
		this.setWidth(350);

		contactListeners = new ArrayList<ContactListener>();
		ContactEditPanel createContact = new ContactEditPanel(false);
		createContact.setHeaderVisible(false);
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
		
		Button cancelBtn = new Button(i18n.cancel());
		cancelBtn.setIcon(IconHelper.createPath("images/cancel.gif"));
		cancelBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				hide();
			}
		});
		createContact.addButton(cancelBtn);
		
		this.add(createContact);
	}

	/**
	 * Listen for when a contact is added
	 * @param l
	 */
	public void addContactListener(ContactListener l) {
		contactListeners.add(l);
	}
	
	/**
	 * @see {@link #addContactListener(ContactListener)}
	 * @param l
	 */
	public void removeContactListener(ContactListener l) {
		contactListeners.remove(l);
	}
	
	/**
	 * Fire the contact listeners, to inform of a new contact
	 * @param event
	 */
	private void fireContactEvent(ContactEvent event) {
		for (ContactListener l : contactListeners)
			l.handleEvent(event);
	}
}
