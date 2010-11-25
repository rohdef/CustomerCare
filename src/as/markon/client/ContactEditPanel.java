package as.markon.client;

import java.util.ArrayList;

import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;

import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;


public class ContactEditPanel extends FormPanel {
	private ListStore<Contact> emptyStore;
	private ComboBox<Contact> contactsBox;
	private TextField<String> nameFld;
	private TextField<String> titleFld;
	private TextField<String> phoneFld;
	private TextField<String> mailFld;
	private CheckBox acceptsMailsBox;
	private TextArea commentFld;
	private FormBinding contactBinding;
	
	private DataServiceAsync dataService = Global.getInstance().getDataService();

	public ContactEditPanel() {
		this.setHeading("Kontakter");

		emptyStore = new ListStore<Contact>();

		contactsBox = new ComboBox<Contact>();
		contactsBox.setFieldLabel("Kontaktliste");
		contactsBox.setDisplayField("contactname");
		contactsBox.setTypeAhead(true);
		contactsBox.setStore(emptyStore);
		contactsBox.setTriggerAction(TriggerAction.ALL);
		this.add(contactsBox);

		nameFld = new TextField<String>();
		nameFld.setBorders(false);
		nameFld.setFieldLabel("Navn");
		nameFld.setName("contactname");
		this.add(nameFld);

		titleFld = new TextField<String>();
		titleFld.setBorders(false);
		titleFld.setFieldLabel("Titel");
		titleFld.setName("title");
		this.add(titleFld);

		phoneFld = new TextField<String>();
		phoneFld.setBorders(false);
		phoneFld.setFieldLabel("Telefon");
		phoneFld.setName("phone");
		this.add(phoneFld);

		mailFld = new TextField<String>();
		mailFld.setBorders(false);
		mailFld.setFieldLabel("Mail");
		mailFld.setName("mail");
		this.add(mailFld);

		acceptsMailsBox = new CheckBox();
		acceptsMailsBox.setFieldLabel("Ønsker mails");
		acceptsMailsBox.setName("acceptsmails");
		this.add(acceptsMailsBox);

		commentFld = new TextArea();
		commentFld.setBorders(false);
		commentFld.setFieldLabel("Kommentarer");
		commentFld.setName("comments");
		this.add(commentFld);

		contactBinding = new FormBinding(this, true);

		contactsBox.addSelectionChangedListener(
						new SelectionChangedListener<Contact>() {
					@Override
					public void selectionChanged(SelectionChangedEvent<Contact> se) {
						contactBinding.bind(se.getSelectedItem());
					}
				});
	}
	
	public void bindCompany(Company company) {
		contactsBox.setStore(emptyStore);

		dataService.getContactsFor(Global.getInstance().getCurrentSalesman(), company,
			new AsyncCallback<ArrayList<Contact>>() {
				public void onSuccess(ArrayList<Contact> result) {
					ListStore<Contact> contactStore = new ListStore<Contact>();
					contactStore.add(result);
					contactsBox.setStore(contactStore);
				}

				public void onFailure(Throwable caught) {
					throw new RuntimeException(caught);
				}
			});

	}
	
	
	public void unbindCompany() {
		contactsBox.setStore(emptyStore);
	}
}
