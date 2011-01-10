package dk.rohdef.client.i18n;

import com.google.gwt.i18n.client.Messages;;

public interface CustomerCareI18n extends Messages {
	@DefaultMessage("{0} - CustomerCare")
	public String pageTitle(String companyName);
}
