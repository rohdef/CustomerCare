package as.markon.viewmodel;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class Contact extends BaseModelData {
	private static final long serialVersionUID = 1L;

	public String getName() {
		return this.get("contactname");
	}
	
	public void setName(String name) {
		this.set("contactname", name);
	}
}
