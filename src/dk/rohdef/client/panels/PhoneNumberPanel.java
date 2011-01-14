package dk.rohdef.client.panels;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.PropertyEditor;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;

import dk.rohdef.client.i18n.CustomerCareI18n;
import dk.rohdef.client.services.Global;
import dk.rohdef.client.specialtypes.VType;
import dk.rohdef.client.specialtypes.VTypeValidator;

public class PhoneNumberPanel extends FormPanel {
	private CustomerCareI18n i18n;
	
	public PhoneNumberPanel() {
		i18n = Global.getInstance().getI18n();
		
		this.setBorders(false);
		this.setFrame(false);
		this.setHeaderVisible(false);
		this.setBodyBorder(false);
		this.setPadding(0);
		
		Label phoneTitle = new Label("Telefon(er)");
		phoneTitle.setStyleAttribute("font-weight", "bold");
		this.add(phoneTitle);
		
		final TextField<String> phoneFld = getPhoneFld();
		this.add(phoneFld);
		
		Button addPhoneBtn = new Button();
		// TODO internationalize
		addPhoneBtn.setText("Tilfoej");
		addPhoneBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				add(getPhoneFld());
				layout();
			}
		});
		Menu phoneFieldMenu = new Menu();
		MenuItem removePhoneMenuItem = new MenuItem("Fjern");
		phoneFieldMenu.add(removePhoneMenuItem);
		
		this.setButtonAlign(HorizontalAlignment.LEFT);
		this.addButton(addPhoneBtn);
		
		this.addButton(new Button("Addnumber", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				phoneFld.setValue("+4522112735");
			}
		}));
		
		this.addButton(new Button("Print", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				System.out.println(phoneFld.getValue());
			}
		}));
	}
	
	public void addPhoneNumber(String number) {
		
	}
	
	public TextField<String> getPhoneFld() {
		TextField<String> phoneFld = new TextField<String>();
		phoneFld.setFieldLabel(i18n.phone());
		phoneFld.setName("phone");
		phoneFld.setValidator(new VTypeValidator(VType.PHONE));
		phoneFld.setAutoValidate(true);
		phoneFld.setPropertyEditor(getPhonePropertyEditor());
		
		return phoneFld;
	}
	
	public PropertyEditor<String> getPhonePropertyEditor() {
		return new PropertyEditor<String>() {
			public String getStringValue(String value) {
				String output = value;
				int charCount = output.length();
				
				String country = value.substring(0, 3);
				if (charCount == 11) {
					output = country + " " + value.substring(3, 7) + " "
					+ value.substring(7); 
				} else if (charCount < 7) {
					output = country + " " + value.substring(3);
				} else {
					int restcounter = 3;
					
					output = country + " ";
					if (charCount%3 == 2) {
						output += value.substring(restcounter, restcounter+2) + " ";
						restcounter += 2;
					} else if (charCount%3 == 1) {
						output += value.substring(restcounter, restcounter+4) + " ";
						restcounter += 4;
					}
					
					while (restcounter < charCount) {
						output += value.substring(restcounter, restcounter+3) + " ";
						restcounter += 3;
					}
					output = output.trim();
				}
				
				return output;
			}
			
			public String convertStringValue(String value) {
				String output = value;
				output = output.replaceAll("[ -]*", "");
				
				if (value.startsWith("00"))
					output = "+" + output.substring(2);
				else if (!value.startsWith("+"))
					output = "+45"+output;
				
				return output;
			}
		};
	}
}
