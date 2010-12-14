package as.markon.viewmodel;

import com.extjs.gxt.ui.client.data.BaseModel;

public class MailRecipient extends BaseModel {
	private static final long serialVersionUID = 1L;

	public MailRecipient(String name, String mail) {
		setName(name);
		setMail(mail);
	}
	
	public String getName() {
		return get("name");
	}

	public void setName(String name) {
		set("name", name);
	}

	public String getMail() {
		return get("mail");
	}

	public void setMail(String mail) {
		set("mail", mail);
	}
}
