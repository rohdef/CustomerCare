package as.markon.client.events;

import as.markon.viewmodel.Contact;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.EventType;

public class NewContactEvent extends BaseEvent {
	private Contact contact;
	
	public NewContactEvent(EventType type, Contact contact) {
		super(type);
		
		if (contact == null)
			throw new NullPointerException("The contact can not be null");
		
		this.contact = contact;
	}
	
	public Contact getContact() {
		return contact;
	}
}
