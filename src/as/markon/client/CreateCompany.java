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
import com.extjs.gxt.ui.client.data.ChangeEvent;
import com.extjs.gxt.ui.client.data.ChangeEventSource;
import com.extjs.gxt.ui.client.data.ChangeEventSupport;
import com.extjs.gxt.ui.client.data.ChangeListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
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
	public static EventType CompanyCreated = new EventType();
	
	private DataServiceAsync dataService;
	private LoadingDialog loader = new LoadingDialog();
	private boolean loadingCities = true, loadingTrades = true;
	
	private Company newCompany;
	private ListStore<Contact> contactStore;
	private Grid<Contact> contactGrid;

	public CreateCompany() {
		dataService = Global.getInstance().getDataService();
		
		newCompany = new Company();
		newCompany.setImportance(Importance.I);
		newCompany.setAcceptsMails(false);
		
		contactStore = new ListStore<Contact>();
		
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
	
	@Override
	public void show() {
		super.show();
		checkLoader();
	}
	
	private void checkLoader() {
		if (loadingCities || loadingTrades)
			loader.show();
		else
			loader.hide();
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
				
				loadingCities = false;
				checkLoader();
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
		phoneFld.setValidator(new VTypeValidator(VType.PHONE));
		formPanel.add(phoneFld);

		TextField<String> mailFld = new TextField<String>();
		mailFld.setFieldLabel("Mail:");
		mailFld.setName("mail");
		mailFld.setValidator(new VTypeValidator(VType.EMAIL));
		formPanel.add(mailFld);
		
		CheckBox acceptsMailsBox = new CheckBox();
		acceptsMailsBox.setFieldLabel("Ønsker mails");
		acceptsMailsBox.setName("acceptsmails");
		formPanel.add(acceptsMailsBox);

		final ListStore<Trade> tradeStore = new ListStore<Trade>();

		dataService.getTrades(new AsyncCallback<ArrayList<Trade>>() {
			public void onSuccess(ArrayList<Trade> result) {
				tradeStore.removeAll();
				tradeStore.add(result);
				
				loadingTrades = false;
				checkLoader();
			}

			public void onFailure(Throwable caught) {
			}
		});
		
		ComboBox<Trade> tradeBox = new ComboBox<Trade>();
		tradeBox.setFieldLabel("Branche:");
		tradeBox.setDisplayField("trade");
		tradeBox.setName("trade");
		tradeBox.setTypeAhead(true);
		tradeBox.setStore(tradeStore);
		tradeBox.setTriggerAction(TriggerAction.ALL);
		tradeBox.setValidator(new VTypeValidator(VType.ALPHABET));
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

		formPanel.addButton(getCreateCompanyButton());
		
		formPanel.addButton(getCancelButton());
		
		return formPanel;
	}
	
	private FormPanel createNewContactsPanel() {
		final FormPanel contactsPanel = new FormPanel();
		contactsPanel.setHeading("Kontaktoplysninger");
		contactsPanel.setWidth("50%");
		contactsPanel.setBorders(false);
		
		final TextField<String> nameFld = new TextField<String>();
		nameFld.setBorders(false);
		nameFld.setFieldLabel("Navn");
		nameFld.setAllowBlank(false);
		contactsPanel.add(nameFld);

		final TextField<String> titleFld = new TextField<String>();
		titleFld.setBorders(false);
		titleFld.setFieldLabel("Titel");
		titleFld.setValidator(new VTypeValidator(VType.ALPHABET));
		contactsPanel.add(titleFld);

		final TextField<String> phoneFld = new TextField<String>();
		phoneFld.setBorders(false);
		phoneFld.setFieldLabel("Telefon");
		phoneFld.setValidator(new VTypeValidator(VType.PHONE));
		contactsPanel.add(phoneFld);

		final TextField<String> mailFld = new TextField<String>();
		mailFld.setBorders(false);
		mailFld.setFieldLabel("Mail");
		mailFld.setValidator(new VTypeValidator(VType.PHONE));
		contactsPanel.add(mailFld);
		
		final CheckBox acceptsMailsBox = new CheckBox();
		acceptsMailsBox.setFieldLabel("Ønsker mails");
		contactsPanel.add(acceptsMailsBox);

		final TextArea commentFld = new TextArea();
		commentFld.setBorders(false);
		commentFld.setFieldLabel("Kommentarer");
		contactsPanel.add(commentFld);
		
		Button addContactBtn = new Button("Tilfoej kontakt", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				Contact newContact = new Contact();
				newContact.setName(nameFld.getValue());
				newContact.setTitle(titleFld.getValue());
				newContact.setPhone(phoneFld.getValue());
				newContact.setMail(mailFld.getValue());
				newContact.setAcceptsMails(acceptsMailsBox.getValue());
				newContact.setComments(commentFld.getValue());

				contactStore.add(newContact);
				contactsPanel.clear();
			}
		});
		addContactBtn.setType("submit");
		contactsPanel.addButton(addContactBtn);
		
		return contactsPanel;
	}
	
	private ContentPanel getContactList() {
		ContentPanel contactsPanel = new ContentPanel();
		contactsPanel.setWidth("50%");
		contactsPanel.setHeading("Kontakter");
		contactsPanel.setHeight(130);
		
		ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		configs.add(new ColumnConfig("contactname", "Navn", 125));
		configs.add(new ColumnConfig("title", "Titel", 75));
		
		ColumnModel cm = new ColumnModel(configs);
		
		contactGrid = new Grid<Contact>(contactStore, cm);
		contactGrid.setBorders(false);
		contactGrid.setStripeRows(true);
		contactGrid.setHeight(120);
		
		contactGrid.getView().setEmptyText("Ingen kontakter oprettet endnu.");
		
		contactsPanel.add(contactGrid);
		
		return contactsPanel;
	}
	
	public Company getNewCompany() {
		return newCompany;
	}
	
	private ChangeEventSupport changeEventSupport = new ChangeEventSupport();
	public void addChangeListener(ChangeListener... listener) {
		changeEventSupport.addChangeListener(listener);
	}
	
	public void removeChangeListener(ChangeListener... listener) {
		changeEventSupport.removeChangeListener(listener);
	}

	public Button getCreateCompanyButton() {
		return new Button("Opret firma",
				new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (contactStore.getCount() > 0)
					createCompany();
				else {
					Dialog confirmDialog = new Dialog();
					confirmDialog.setButtons(Dialog.YESNO);
					confirmDialog.setHideOnButtonClick(true);
					
					confirmDialog.setHeading("Ønsker du at oprette som kundekandidat?");
					confirmDialog.addText("Vil du oprette en virksomhed uden kontakter.");
					confirmDialog.addText("En virksomhed uden kontakter vil dukke op som" +
							"en kunde kanidat.");
					
					confirmDialog.getButtonById(Dialog.NO).setText("Fortryd");
					confirmDialog.getButtonById(Dialog.YES).setText("Opret kundekandidat");
					confirmDialog.getButtonById(Dialog.YES).addSelectionListener(
							new SelectionListener<ButtonEvent>() {
								@Override
								public void componentSelected(ButtonEvent ce) {
									createCompany();
								}
							});
					confirmDialog.show();
				}
			}
		});
	}
	
	public Button getCancelButton() {
		return new Button("Anuller", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				fireEvent(Events.Close);
			}
		});
	}
	
	private void createCompany() {
		loader.show();
		
		dataService.createCompany(newCompany,
				new ArrayList<Contact>(contactStore.getModels()),
				Global.getInstance().getCurrentSalesman(),
				new AsyncCallback<Integer>() {
			public void onSuccess(Integer result) {
				newCompany.set("companyid", result);
				newCompany.set("prospect", contactStore.getCount()==0);
				
				if (newCompany.getTrade() == null) {
					Trade noTrade = new Trade();
					noTrade.setTrade("Ingen branche valgt");
					newCompany.setTrade(noTrade);
				}
				
				changeEventSupport.notify(
						new ChangeEvent(ChangeEventSource.Add, newCompany));
				
				loader.hide();
				fireEvent(Events.Close);
			}
			
			public void onFailure(Throwable caught) {
				throw new RuntimeException(caught);
			}
		});
	}
}
