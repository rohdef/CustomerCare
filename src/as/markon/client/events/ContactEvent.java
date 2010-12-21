package as.markon.client.events;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.EventType;

import as.markon.viewmodel.Contact;

public class ContactEvent extends BaseEvent {
	private static final int NEW_CONTACT = 570051, CHANGED_CONTACT = 570052,
		DELETED_CONTACT = 570053;
	public static final EventType NEW_CONTACT_TYPE = new EventType(NEW_CONTACT);
	public static final EventType CHANGED_CONTACT_TYPE = new EventType(CHANGED_CONTACT);
	public static final EventType DELETED_CONTACT_TYPE = new EventType(DELETED_CONTACT);
	
	private Contact contact;
	private Contact oldContact;
	
	public ContactEvent(EventType type, Contact contact) {
		this(type, contact, null);
	}
	
	public ContactEvent(EventType type, Contact contact, Contact oldContact) {
		super(type);
		
		if (contact == null)
			throw new NullPointerException("The contact must not be null");

		this.contact = contact;
		this.oldContact = oldContact;
	}

	public Contact getContact() {
		return contact;
	}

	public Contact getOldContact() {
		return oldContact;
	}

}
