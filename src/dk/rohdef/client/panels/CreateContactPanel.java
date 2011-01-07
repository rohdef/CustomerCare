package dk.rohdef.client.panels;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;

import dk.rohdef.client.events.ContactEvent;
import dk.rohdef.client.events.ContactListener;
import dk.rohdef.client.specialtypes.VType;
import dk.rohdef.client.specialtypes.VTypeValidator;
import dk.rohdef.viewmodel.Contact;

public class CreateContactPanel extends FormPanel {
	private ArrayList<ContactListener> newContactListeners =
		new ArrayList<ContactListener>();

	public CreateContactPanel() {
		this.setHeading("Kontaktoplysninger");
		this.setWidth("50%");
		this.setBorders(false);
		this.setFrame(false);
		
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

				fireNewContactEvent(new ContactEvent(ContactEvent.NEW_CONTACT_TYPE, newContact));
				clear();
			}
		});
		addContactBtn.setIcon(IconHelper.createPath("images/user_add.gif"));
		addContactBtn.setType("submit");
		this.addButton(addContactBtn);
	}

	public void addNewContactListener(ContactListener listener) {
		newContactListeners.add(listener);
	}
	
	public void removeNewContactListener(ContactListener listener) {
		newContactListeners.remove(listener);
	}
	
	private void fireNewContactEvent(ContactEvent event) {
		for (ContactListener l : newContactListeners) {
			l.handleEvent(event);
		}
	}
}
