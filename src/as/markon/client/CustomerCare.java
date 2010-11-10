package as.markon.client;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class CustomerCare implements EntryPoint {
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		ButtonBar b = new ButtonBar();
		
		b.add(new Button("Telefonen ringer", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				MessageBox.confirm("Svaret er", "Markon det John", null);
			}
		}));
		
		RootPanel.get().add(b);
	}
}
