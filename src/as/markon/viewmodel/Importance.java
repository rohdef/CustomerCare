package as.markon.viewmodel;

public enum Importance {
	A("Guldkunde"),
	B("Soelvkunde"),
	C("Broncekunde");
	
	private String value;

	Importance(String value) {
		this.value = value;
	}
	
	public String toString() {
		return value;
	}
}
