package dk.rohdef.viewmodel;

import com.extjs.gxt.ui.client.data.BaseModel;

public class LabelRecipient extends BaseModel {
	private static final long serialVersionUID = 1L;
	
	public LabelRecipient(){}
	
	public LabelRecipient(String name, BaseModel recipient) {
		setName(name);
		
		if (recipient instanceof Contact) {
			setContact((Contact)recipient);
			setCompany(null);
		} else if (recipient instanceof Company) {
			setContact(null);
			setCompany((Company)recipient);
		} else {
			throw new IllegalArgumentException("The recipient must be either an instance of" +
					" company or contact.");
		}
	}
	
	public String getName() {
		return get("name");
	}

	public void setName(String name) {
		set("name", name);
	}

	public Contact getContact() {
		return get("contact");
	}
	
	public void setContact(Contact contact) {
		set("contact", contact);
	}
	
	public Company getCompany() {
		return get("company");
	}
	
	public void setCompany(Company company) {
		set("company", company);
	}
}
