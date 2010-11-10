package as.markon.viewmodel;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class Company extends BaseModelData {
	private static final long serialVersionUID = 1L;
	
	public String getName() {
		return this.get("companyname");
	}
	
	public void setName(String name) {
		this.set("companyname", name);
	}
	
	public String getCity() {
		return this.get("city");
	}
	
	public void setCity(String city) {
		this.set("city", city);
	}
}
