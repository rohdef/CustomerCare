package as.markon.client;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

public class LoadingDialog extends Window {

	public LoadingDialog() {
		setModal(true);
		setHeading("Henter data, vent venligst");
		setLayout(new RowLayout(Orientation.VERTICAL));
		
		this.addText("Vent venligst, mens systemet henter data.");
		
		ProgressBar loading = new ProgressBar();
		loading.auto();
		loading.setInterval(100);
		this.add(loading);
		
		this.setClosable(false);
	}
}
