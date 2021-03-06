package dk.rohdef.client.events;


import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.EventType;

import dk.rohdef.viewmodel.Company;

public class DeleteCompanyEvent extends BaseEvent {
	private Company company;
	
	public DeleteCompanyEvent(EventType type, Company company) {
		super(type);

		if (company == null)
			throw new NullPointerException("The company has to be set to a value!");
		
		this.company = company;
	}
	
	public Company getCompany() {
		return company;
	}
}
