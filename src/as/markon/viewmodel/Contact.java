package as.markon.viewmodel;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.google.gwt.user.client.rpc.IsSerializable;

public class Contact extends BaseModelData implements IsSerializable {
	private static final long serialVersionUID = 1L;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Contact other = (Contact) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}

	public Contact() {
	}
	
	public String getName() {
		return get("contactname");
	}

	public void setName(String contactname) {
		set("contactname", contactname);
	}

	public String getTitle() {
		return get("title");
	}

	public void setTitle(String title) {
		set("title", title);
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

	public String getComments() {
		return get("comments");
	}

	public void setComments(String comments) {
		set("comments", comments);
	}
	
	public Boolean getAcceptsMails() {
		return get("acceptsmails");
	}

	public void setAcceptsMails(Boolean acceptsmails) {
		set("acceptsmails", acceptsmails);
	}
}
