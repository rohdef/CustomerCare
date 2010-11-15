package as.markon.viewmodel;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class Trade extends BaseModelData {
	private static final long serialVersionUID = 1L;

	public String getTrade() {
		return get("trade");
	}
	
	public void setTrade(String trade) {
		set("trade", trade);
	}
}
