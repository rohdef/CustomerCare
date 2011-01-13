package dk.rohdef.client;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

import dk.rohdef.client.i18n.CustomerCareI18n;
import dk.rohdef.client.services.Global;

/**
 * A simple loading dialog for when the system is fetching data from the server.
 * @author Rohde Fischer <rohdef@rohdef.dk>
 */
public class LoadingDialog extends Window {
	private CustomerCareI18n i18n;

	/**
	 * 
	 */
	public LoadingDialog() {
		i18n = Global.getInstance().getI18n();
		
		setModal(true);
		setHeading(i18n.loadingTitle());
		this.setIcon(IconHelper.createPath("images/time.gif"));
		setLayout(new RowLayout(Orientation.VERTICAL));
		
		this.addText(i18n.loadingPleaseWait());
		
		ProgressBar loading = new ProgressBar();
		loading.auto();
		loading.setInterval(100);
		this.add(loading);
		
		this.setClosable(false);
	}
}
