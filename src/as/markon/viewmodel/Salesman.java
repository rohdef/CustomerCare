package as.markon.viewmodel;

import com.extjs.gxt.ui.client.data.BaseModel;

public class Salesman extends BaseModel {
	private static final long serialVersionUID = 1L;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getMail() == null) ? 0 : getMail().hashCode());
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
		Salesman other = (Salesman) obj;
		if (get("salesmanid")  == null) {
			if (other.get("salesmanid") != null)
				return false;
		} else if (!get("salesmanid").equals(other.get("salesmanid")))
			return false;
		return true;
	}

	public String getSalesman() {
		return get("salesman");
	}

	public void setSalesman(String salesman) {
		set("salesman", salesman);
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
}
