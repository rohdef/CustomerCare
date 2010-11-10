package as.markon.client;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.custom.Portal;
import com.extjs.gxt.ui.client.widget.custom.Portlet;
import com.google.gwt.user.client.Element;

public class Login extends LayoutContainer {

	public Login() {
	}

	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);
		
		ContentPanel panel = new ContentPanel();
		panel.setBodyStyle("padding: 0.5em;");
		panel.setWidth("80%");
		panel.setBorders(true);
		panel.setHeading("Hvis kundekartotek vil du se?");

		
		Portal content = new Portal(3);
		content.setColumnWidth(0, .33);
		content.setColumnWidth(1, .33);
		content.setColumnWidth(2, .33);
//		content.setWidth("80%");
		
		content.add(new LoginItem("Markon det John1"), 0);
		content.add(new LoginItem("Markon det John2"), 1);
		content.add(new LoginItem("Markon det John3"), 2);
		content.add(new LoginItem("Markon det John4"), 0);
		content.add(new LoginItem("Markon det John5"), 1);
		content.add(new LoginItem("Markon det John6"), 2);
		content.add(new LoginItem("Markon det John7"), 0);
		content.add(new LoginItem("Markon det John8"), 1);
		
		panel.add(content);
		
		this.add(panel);
	}
	
	private class LoginItem extends Portlet {
		public LoginItem(String person) {
			this.setHeading(person);
			
			Button b = new Button();
			b.setText(person);
			
			this.add(b);
		}
	}
}
