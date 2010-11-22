package as.markon.viewmodel;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class Company extends BaseModelData {
	private static final long serialVersionUID = 1L;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getCompanyName() == null) ? 0 : getCompanyName().hashCode());
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
		
		Company other = (Company) obj;
		if (getCompanyName() == null) {
			if (other.getCompanyName() != null)
				return false;
		} else if (!getCompanyName().equals(other.getCompanyName()))
			return false;
		
		if (getPostal() != other.getPostal())
			return false;
		
		if (getAddress() == null) {
			if (other.getAddress() != null)
				return false;
		} else {
			if (other.getAddress() == null)
				return false;
			if (!getAddress().equals(other.getAddress()))
				return false;
		}
		
		if (getPhone() == null) {
			if (other.getPhone() != null)
				return false;
		} else if (!getPhone().equals(other.getPhone()))
			return false;
		
		return true;
	}

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
	
	public City getCompanyCity() {
		return get("companycity");
	}
	
	public void setCompanyCity(City city) {
		set("companycity", city);
	}

	public String getCity() {
		return get("city");
	}

	public void setCity(String city) {
		set("city", city);
	}
	
	public int getPostal() {
		return get("postal");
	}
	
	public void setPostal(int postal) {
		set("postal", postal);
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

	public Trade getTrade() {
		return get("trade");
	}
	
	public void setTrade(Trade trade) {
		set("trade", trade);
	}
}
