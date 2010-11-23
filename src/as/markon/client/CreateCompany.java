package as.markon.client;

import java.util.ArrayList;
import java.util.Arrays;

import as.markon.viewmodel.City;
import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;
import as.markon.viewmodel.Importance;
import as.markon.viewmodel.Trade;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CreateCompany extends LayoutContainer {
	private DataServiceAsync dataService;
	private Company newCompany;
	private ArrayList<Contact> contacts;
	private ListStore<Contact> contactStore;

	public CreateCompany() {
		dataService = Global.getInstance().getDataService();
		
		newCompany = new Company();
		newCompany.setImportance(Importance.I);
		
		contacts = new ArrayList<Contact>();
		contactStore = new ListStore<Contact>();
		contactStore.setMonitorChanges(true);
		
		this.setLayout(new HBoxLayout());
		
		this.add(createNewCompanyPanel());
		
		LayoutContainer contacts = new LayoutContainer();
		contacts.setAutoWidth(true);
		contacts.setHeight(470);
		contacts.setLayout(new VBoxLayout());
		contacts.add(createNewContactsPanel(), new VBoxLayoutData());
		contacts.add(getContactList(), new VBoxLayoutData());
		
		this.add(contacts);
	}

	private FormPanel createNewCompanyPanel() {
		final FormPanel formPanel = new FormPanel();
		formPanel.setAutoHeight(true);
		formPanel.setWidth("50%");
		formPanel.setHeading("Indtast virksomhedsoplysninger");

		TextField<String> companynameFld = new TextField<String>();
		companynameFld.setFieldLabel("Firmanavn:");
		companynameFld.setName("companyname");
		companynameFld.setAllowBlank(false);
		formPanel.add(companynameFld);

		TextField<String> addressFld = new TextField<String>();
		addressFld.setFieldLabel("Adresse:");
		addressFld.setName("address");
		formPanel.add(addressFld);

		final ListStore<City> cityStore = new ListStore<City>();
		cityStore.setMonitorChanges(true);
		dataService.getCities(new AsyncCallback<ArrayList<City>>() {
			public void onSuccess(ArrayList<City> result) {
				cityStore.add(result);
				cityStore.sort("postal", SortDir.ASC);
			}

			public void onFailure(Throwable caught) {
				throw new RuntimeException(caught);
			}
		});
		
		final ComboBox<City> postalBox = new ComboBox<City>();
		postalBox.setFieldLabel("Postnummer");
		postalBox.setDisplayField("postal");
		postalBox.setTypeAhead(true);
		postalBox.setStore(cityStore);
		postalBox.setTriggerAction(TriggerAction.ALL);
		formPanel.add(postalBox);

		final ComboBox<City> cityBox = new ComboBox<City>();
		cityBox.setFieldLabel("By");
		cityBox.setDisplayField("cityname");
		cityBox.setTypeAhead(true);
		cityBox.setStore(cityStore);
		cityBox.setTriggerAction(TriggerAction.ALL);
		formPanel.add(cityBox);

		postalBox.addSelectionChangedListener(new SelectionChangedListener<City>() {
					@Override
					public void selectionChanged(SelectionChangedEvent<City> se) {
						newCompany.setPostal(se.getSelectedItem().getPostal());
						
						if (cityBox.getSelection().equals(se.getSelection()))
							return;
						cityBox.setSelection(se.getSelection());
					}
				});

		cityBox.addSelectionChangedListener(new SelectionChangedListener<City>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<City> se) {
				if (postalBox.getSelection().equals(se.getSelection()))
					return;
				postalBox.setSelection(se.getSelection());
			}
		});

		TextField<String> phoneFld = new TextField<String>();
		phoneFld.setFieldLabel("Telefon:");
		phoneFld.setName("phone");
		formPanel.add(phoneFld);

		TextField<String> mailFld = new TextField<String>();
		mailFld.setFieldLabel("Mail:");
		mailFld.setName("mail");
		formPanel.add(mailFld);

		final ListStore<Trade> tradeStore = new ListStore<Trade>();

		dataService.getTrades(new AsyncCallback<ArrayList<Trade>>() {
			public void onSuccess(ArrayList<Trade> result) {
				tradeStore.removeAll();
				tradeStore.add(result);
			}

			public void onFailure(Throwable caught) {
				throw new RuntimeException(caught);
			}
		});

		ComboBox<Trade> tradeBox = new ComboBox<Trade>();
		tradeBox.setFieldLabel("Branche:");
		tradeBox.setDisplayField("trade");
		tradeBox.setName("trade");
		tradeBox.setTypeAhead(true);
		tradeBox.setStore(tradeStore);
		tradeBox.setTriggerAction(TriggerAction.ALL);
		formPanel.add(tradeBox);

		final SimpleComboBox<Importance> importanceBox = new SimpleComboBox<Importance>();
		importanceBox.setFieldLabel("Gruppe:");
		importanceBox.add(Arrays.asList(Importance.values()));
		importanceBox.setTriggerAction(TriggerAction.ALL);
		importanceBox.setSimpleValue(newCompany.getImportance());
		
		importanceBox.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<Importance>>() {
					@Override
					public void selectionChanged(
							SelectionChangedEvent<SimpleComboValue<Importance>> se) {
						newCompany.setImportance(importanceBox.getSimpleValue());
					}
				});
		formPanel.add(importanceBox);

		TextArea commentsFld = new TextArea();
		commentsFld.setFieldLabel("Kommentarer:");
		commentsFld.setName("comments");
		formPanel.add(commentsFld);

		FormBinding binding = new FormBinding(formPanel);
		binding.autoBind();
		binding.bind(newCompany);

		formPanel.addButton(new Button("Opret firma",
				new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						dataService.createCompany(newCompany, null, Global.getInstance().getCurrentSalesman(),
								new AsyncCallback<Integer>() {
							public void onSuccess(Integer result) {
								newCompany.set("companyid", result);
								
								fireEvent(Events.Add);
								fireEvent(Events.Close);
							}
							
							public void onFailure(Throwable caught) {
								throw new RuntimeException(caught);
							}
						});
					}
				}));
		
		formPanel.addButton(new Button("Anuller", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				fireEvent(Events.Close);
			}
		}));

		return formPanel;
	}
	
	private FormPanel createNewContactsPanel() {
		final FormPanel contactsPanel = new FormPanel();
		contactsPanel.setHeading("Kontaktoplysninger");
		contactsPanel.setWidth("50%");
		contactsPanel.setBorders(false);
		
		TextField<String> nameFld = new TextField<String>();
		nameFld.setBorders(false);
		nameFld.setFieldLabel("Navn");
		nameFld.setName("contactname");
		contactsPanel.add(nameFld);

		TextField<String> titleFld = new TextField<String>();
		titleFld.setBorders(false);
		titleFld.setFieldLabel("Titel");
		titleFld.setName("title");
		contactsPanel.add(titleFld);

		TextField<String> phoneFld = new TextField<String>();
		phoneFld.setBorders(false);
		phoneFld.setFieldLabel("Telefon");
		phoneFld.setName("phone");
		contactsPanel.add(phoneFld);

		TextField<String> mailFld = new TextField<String>();
		mailFld.setBorders(false);
		mailFld.setFieldLabel("Mail");
		mailFld.setName("mail");
		contactsPanel.add(mailFld);
		
		CheckBox acceptsMailsBox = new CheckBox();
		acceptsMailsBox.setFieldLabel("Ønsker mails");
		acceptsMailsBox.setName("acceptsmails");
		contactsPanel.add(acceptsMailsBox);

		TextArea commentFld = new TextArea();
		commentFld.setBorders(false);
		commentFld.setFieldLabel("Kommentarer");
		commentFld.setName("comments");
		contactsPanel.add(commentFld);
		
		final FormBinding binding = new FormBinding(contactsPanel);
		binding.autoBind();
		Contact emptyContact = new Contact();
		emptyContact.setName("");
		emptyContact.setTitle("");
		emptyContact.setPhone("");
		emptyContact.setMail("");
		emptyContact.setAcceptsMails(false);
		emptyContact.setComments("");
		binding.bind(emptyContact);
		
		contactsPanel.addButton(new Button("Tilfoej kontakt", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				Contact newContact = (Contact) binding.getModel();

				Contact emptyContact = new Contact();
				emptyContact.setName("");
				emptyContact.setTitle("");
				emptyContact.setPhone("");
				emptyContact.setMail("");
				emptyContact.setAcceptsMails(false);
				emptyContact.setComments("");
				binding.bind(emptyContact);
				
				contacts.add(newContact);
				contactStore.add(newContact);
			}
		}));
		
		return contactsPanel;
	}
	
	private ContentPanel getContactList() {
		ContentPanel contactsPanel = new ContentPanel();
		contactsPanel.setWidth("50%");
		contactsPanel.setHeading("Kontakter");
		contactsPanel.setHeight(100);
		
		ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		configs.add(new ColumnConfig("contactname", "Navn", 125));
		configs.add(new ColumnConfig("title", "Titel", 75));
		
		ColumnModel cm = new ColumnModel(configs);
		
		final Grid<Contact> contactGrid = new Grid<Contact>(contactStore, cm);
		contactGrid.setStripeRows(true);
		contactGrid.setBorders(false);
		
		contactsPanel.add(contactGrid);
		
		return contactsPanel;
	}
	
	public Company getNewCompany() {
		return newCompany;
	}
}
