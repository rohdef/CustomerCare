package as.markon.client;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;

public class ColumnTest extends Composite {

	public ColumnTest() {
		CellTable<Contact> table = new CellTable<Contact>();
		table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
		
		TextColumn<Contact> nameColumn = new TextColumn<Contact>() {
			@Override
			public String getValue(Contact object) {
				return null;
			}
		};
	}

	private static final List<Contact> CONTACTS = Arrays.asList(
			new Contact("John Doe", new Date(80, 4, 12), "Somewhere", true),
			new Contact("My Self", new Date(85, 6, 5), "Denmark", false),
			new Contact("Dear John", new Date(61, 3, 19), "Broadway", true)
	);
	
	private static class Contact {
		private final String address;
	    private final Date birthday;
	    private final String name;
	    private final boolean editable;
	    
	    public Contact(String name, Date birthday, String address, boolean editable) {
	        this.name = name;
	        this.birthday = birthday;
	        this.address = address;
	        this.editable = editable;
	      }
	}
}
