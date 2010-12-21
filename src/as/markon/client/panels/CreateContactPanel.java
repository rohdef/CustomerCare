package as.markon.client.panels;

import java.util.ArrayList;

import as.markon.client.events.NewContactEvent;
import as.markon.client.events.NewContactListener;
import as.markon.client.specialtypes.VType;
import as.markon.client.specialtypes.VTypeValidator;
import as.markon.viewmodel.Contact;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;

public class CreateContactPanel extends FormPanel {
	private ArrayList<NewContactListener> newContactListeners =
		new ArrayList<NewContactListener>();

	public CreateContactPanel() {
		this.setHeading("Kontaktoplysninger");
		this.setWidth("50%");
		this.setBorders(false);
		
		final TextField<String> nameFld = new TextField<String>();
		nameFld.setBorders(false);
		nameFld.setFieldLabel("Navn");
		nameFld.setAllowBlank(false);
		nameFld.setAutoValidate(true);
		this.add(nameFld);

		final TextField<String> titleFld = new TextField<String>();
		titleFld.setBorders(false);
		titleFld.setFieldLabel("Titel");
		titleFld.setAutoValidate(true);
		titleFld.setValidator(new VTypeValidator(VType.ALPHABET));
		this.add(titleFld);

		final TextField<String> phoneFld = new TextField<String>();
		phoneFld.setBorders(false);
		phoneFld.setFieldLabel("Telefon");
		phoneFld.setAutoValidate(true);
		phoneFld.setValidator(new VTypeValidator(VType.PHONE));
		this.add(phoneFld);

		final TextField<String> mailFld = new TextField<String>();
		mailFld.setBorders(false);
		mailFld.setFieldLabel("Mail");
		mailFld.setAutoValidate(true);
		mailFld.setValidator(new VTypeValidator(VType.EMAIL));
		this.add(mailFld);
		
		final CheckBox acceptsMailsBox = new CheckBox();
		acceptsMailsBox.setFieldLabel("Ønsker mails");
		this.add(acceptsMailsBox);

		final TextArea commentFld = new TextArea();
		commentFld.setBorders(false);
		commentFld.setFieldLabel("Kommentarer");
		this.add(commentFld);
		
		Button addContactBtn = new Button("Tilføj kontakt", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				Contact newContact = new Contact();
				newContact.setName(nameFld.getValue());
				newContact.setTitle(titleFld.getValue());
				newContact.setPhone(phoneFld.getValue());
				newContact.setMail(mailFld.getValue());
				newContact.setAcceptsMails(acceptsMailsBox.getValue());
				newContact.setComments(commentFld.getValue());

				fireNewContactEvent(new NewContactEvent(Events.AfterEdit, newContact));
				clear();
			}
		});
		addContactBtn.setType("submit");
		this.addButton(addContactBtn);
	}

	public void addNewContactListener(NewContactListener listener) {
		newContactListeners.add(listener);
	}
	
	public void removeNewContactListener(NewContactListener listener) {
		newContactListeners.remove(listener);
	}
	
	private void fireNewContactEvent(NewContactEvent event) {
		for (NewContactListener l : newContactListeners) {
			l.handleEvent(event);
		}
	}
}
