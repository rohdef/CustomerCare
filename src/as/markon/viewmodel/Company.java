package as.markon.viewmodel;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class Company extends BaseModelData {
	private static final long serialVersionUID = 1L;
	
	public String getCompanyName() {
		return get("companyname");
	}

	public void setCompanyName(String companyname) {
		set("companyname", companyname);
	}

	public String getAddress() {
		return get("address");
	}

	public void setAddress(String address) {
		set("address", address);
	}

	public String getCity() {
		return get("city");
	}

	public void setCity(String city) {
		set("city", city);
	}

	public String getPhone() {
		return get("phone");
	}

	public void setPhone(String phone) {
		set("phone", phone);
	}

	public String getMail() {
		return get("mail");
	}

	public void setMail(String mail) {
		set("mail", mail);
	}

	public Importance getImportance() {
		return get("importance");
	}

	public void setImportance(Importance importance) {
		set("importance", importance);
	}

	public String getComments() {
		return get("comments");
	}

	public void setComments(String comments) {
		set("comments", comments);
	}

	public ArrayList<Contact> getContacts() {
		return get("contacts");
	}

	public void setContacts(ArrayList<Contact> contacts) {
		set("contacts", contacts);
	}
}
