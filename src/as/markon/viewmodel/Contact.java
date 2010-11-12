package as.markon.viewmodel;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.google.gwt.user.client.rpc.IsSerializable;

public class Contact extends BaseModelData implements IsSerializable {
	private static final long serialVersionUID = 1L;

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

}
