package as.markon.viewmodel;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class Salesman extends BaseModelData {
	private static final long serialVersionUID = 1L;
	
	public int getSalesmanid() {
		return get("salesmanid");
	}

	public void setSalesmanid(int salesmanid) {
		set("salesmanid", salesmanid);
	}

	public String getSalesman() {
		return get("salesman");
	}

	public void setSalesman(String salesman) {
		set("salesman", salesman);
	}

}
