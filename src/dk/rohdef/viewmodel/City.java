package dk.rohdef.viewmodel;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class City extends BaseModelData implements Comparable<City> {
	private static final long serialVersionUID = 1L;
	
	public int getPostal() {
		return get("postal");
	}
	
	public void setPostal(int postal) {
		set("postal", postal);
	}
	
	public String getCity() {
		return get("cityname");
	}
	
	public void setCity(String city) {
		set("cityname", city);
	}
	
	public int compareTo(City o) {
		return this.getPostal()-o.getPostal();
	}
}
