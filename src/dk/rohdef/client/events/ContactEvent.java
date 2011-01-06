package dk.rohdef.client.events;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.EventType;

import dk.rohdef.viewmodel.Company;
import dk.rohdef.viewmodel.Contact;


/**
 * Event for when a contact is changed, possible changes are new, delete and modified. 
 * @author Rohde Fischer <rohdef@rohdef.dk>
 */
public class ContactEvent extends BaseEvent {
	private static final int NEW_CONTACT = 570051, CHANGED_CONTACT = 570052,
		DELETED_CONTACT = 570053;
	public static final EventType NEW_CONTACT_TYPE = new EventType(NEW_CONTACT);
	public static final EventType CHANGED_CONTACT_TYPE = new EventType(CHANGED_CONTACT);
	public static final EventType DELETED_CONTACT_TYPE = new EventType(DELETED_CONTACT);
	
	private Contact contact;
	private Contact oldContact;
	private Company company;
	
	/**
	 * 
	 * @param type is one of the following: ContactEvent.NEW_CONTACT_TYPE, 
	 * ContactEvent.CHANGED_CONTACT_TYPE or DELETED_CONTACT_TYPE
	 * @param contact the contact that has been modified
	 */
	public ContactEvent(EventType type, Contact contact) {
		this(type, contact, null, null);
	}
	
	/**
	 * 
	 * @param type is one of the following: ContactEvent.NEW_CONTACT_TYPE, 
	 * ContactEvent.CHANGED_CONTACT_TYPE or DELETED_CONTACT_TYPE
	 * @param contact the contact that has been modified
	 * @param oldContact the contact before the modifications
	 */
	public ContactEvent(EventType type, Contact contact, Contact oldContact) {
		this(type, contact, oldContact, null);
	}
	
	/**
	 * 
	 * @param type is one of the following: ContactEvent.NEW_CONTACT_TYPE, 
	 * ContactEvent.CHANGED_CONTACT_TYPE or DELETED_CONTACT_TYPE
	 * @param contact the contact that has been modified
	 * @param oldContact the contact before the modifications
	 * @param company the company related to the contact
	 */
	public ContactEvent(EventType type, Contact contact, Contact oldContact, Company company) {
		super(type);
		
		if (contact == null)
			throw new NullPointerException("The contact must not be null");
		if (type == DELETED_CONTACT_TYPE && company == null)
			throw new NullPointerException("The company has to be set, when deleting");

		this.contact = contact;
		this.oldContact = oldContact;
		this.company = company;
	}

	public Contact getContact() {
		return contact;
	}

	/**
	 * This will most likely be null for the add and delete events. Otherwise it should 
	 * contain the contact before modification.
	 * @return
	 */
	public Contact getOldContact() {
		return oldContact;
	}

	/**
	 * This will be likely to be null for add and change events. For delete events, this 
	 * will contain the related company, this will be a safeguard against errors.
	 * @return
	 */
	public Company getCompany() {
		return company;
	}
}
