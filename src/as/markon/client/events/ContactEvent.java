package as.markon.client.events;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.EventType;

import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;

public class ContactEvent extends BaseEvent {
	private static final int NEW_CONTACT = 570051, CHANGED_CONTACT = 570052;
	public static final EventType NEW_CONTACT_TYPE = new EventType(NEW_CONTACT);
	public static final EventType CHANGED_CONTACT_TYPE = new EventType(CHANGED_CONTACT);
	
	private Contact contact;
	private Company company;
	
	public ContactEvent(EventType type, Contact contact) {
		this(type, contact, null);
	}
	
	public ContactEvent(EventType type, Contact contact, Company company) {
		super(type);
		
		if (contact == null)
			throw new NullPointerException("The contact must not be null");

		this.contact = contact;
		this.company = company;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}
}
