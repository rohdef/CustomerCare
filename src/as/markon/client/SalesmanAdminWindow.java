package as.markon.client;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

public class SalesmanAdminWindow extends Window {

	public SalesmanAdminWindow() {
		setHeading("Administrer sælgere");
		setLayout(new RowLayout(Orientation.VERTICAL));
	}
}
