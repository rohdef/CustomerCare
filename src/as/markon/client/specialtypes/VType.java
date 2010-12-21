package as.markon.client.specialtypes;

public enum VType {
	ALPHABET("^[a-zæøåA-ZÆØÅ -\\.,]+$", "Alphabet"), 
	ALPHANUMERIC("^[a-zA-Z0-9_]+$", "Alphanumeric"), 
	NUMERIC("^[0-9]+$", "Numeric"),
	//EMAIL("^(\\w+)([-+.][\\w]+)*@(\\w[-\\w]*\\.){1,5}([A-Za-z]){2,4}$", "Email"),
//	EMAIL("^[.&[^@]]+@[.&[^@]]+\\.[a-z]{2,}$", "Email"),
	EMAIL("^([a-zæøåäëöA-ZÆØÅÄËÖ0-9_.-])+@(([a-zæøåA-ZÆØÅ0-9-])+.)+([a-zA-Z0-9-]{2,})+$", "Email"),
	NAME("^[a-zæøåäöéèA-ZÆØÅÄÖÈÉ]{2,}( [a-zæøåäöéèA-ZÆØÅÄÖÈÉ]{2,})*$", "Name"),
	PHONE("^(((0045|\\+45)( )?)?(\\d{2}( )?\\d{2}(-| | - )?\\d{2}( )?\\d{2}))|" +
			"(?!(00|\\+)45)(00|\\+(\\d{2}))((\\d)+[ -]*)+$", "Phone");
	
	String regex;
	String name;

	VType(String regex, String name) {
		this.regex = regex;
		this.name = name;
	}
}