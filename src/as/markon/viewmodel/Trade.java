package as.markon.viewmodel;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class Trade extends BaseModelData {
	private static final long serialVersionUID = 1L;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getTrade() == null) ? 0 : getTrade().hashCode());
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
		Trade other = (Trade) obj;
		if (getTrade() == null) {
			if (other.getTrade() != null)
				return false;
		} else if (!getTrade().equals(other.getTrade()))
			return false;
		return true;
	}

	public String getTrade() {
		return get("trade");
	}
	
	public void setTrade(String trade) {
		set("trade", trade);
	}
}
