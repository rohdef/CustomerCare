package dk.rohdef.client;

import java.util.ArrayList;
import java.util.Arrays;


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
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

import dk.rohdef.client.events.ContactEvent;
import dk.rohdef.client.events.ContactListener;
import dk.rohdef.client.panels.CreateContactPanel;
import dk.rohdef.client.services.DataServiceAsync;
import dk.rohdef.client.services.Global;
import dk.rohdef.client.specialtypes.VType;
import dk.rohdef.client.specialtypes.VTypeValidator;
import dk.rohdef.viewmodel.City;
import dk.rohdef.viewmodel.Company;
import dk.rohdef.viewmodel.Contact;
import dk.rohdef.viewmodel.Importance;
import dk.rohdef.viewmodel.Trade;

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
		
		contactStore = new ListStore<Contact>();
		
		this.setLayout(new VBoxLayout());
		
		final FormPanel newCompanyPanel = createNewCompanyPanel();
		
		final LayoutContainer contacts = new LayoutContainer();
		contacts.setAutoWidth(true);
		contacts.setHeight(470);
		contacts.setLayout(new HBoxLayout());
		contacts.setVisible(false);
		this.add(contacts);
		
		CreateContactPanel createNewContactPanel = new CreateContactPanel();
		createNewContactPanel.addNewContactListener(new ContactListener() {
			public void handleEvent(ContactEvent be) {
				contactStore.add(be.getContact());
			}
		});
		
		contacts.add(createNewContactPanel, new HBoxLayoutData());
		contacts.add(getContactList(), new HBoxLayoutData());
		
		this.add(newCompanyPanel);
		
		final FormButtonBinding companyButtonBinding = new FormButtonBinding(newCompanyPanel);
		final FormButtonBinding contactButtonBinding = new FormButtonBinding(createNewContactPanel);
		final Button previousBtn = new Button("Forrige");
		final Button nextBtn = new Button("Næste");
		final Button createCompanyBtn = getCreateCompanyButton();
		ButtonBar buttonBar = new ButtonBar();
		
		previousBtn.disable();
		previousBtn.setIcon(IconHelper.createPath("images/arrow_left.gif"));
		previousBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				companyButtonBinding.addButton(nextBtn);
				contactButtonBinding.removeButton(previousBtn);
				contacts.setVisible(false);
				newCompanyPanel.setVisible(true);
				nextBtn.enable();
				previousBtn.disable();
				createCompanyBtn.disable();
			}
		});
		buttonBar.add(previousBtn);
		
		nextBtn.setIcon(IconHelper.createPath("images/arrow_right.gif"));
		nextBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				companyButtonBinding.removeButton(nextBtn);
				contactButtonBinding.addButton(previousBtn);
				newCompanyPanel.setVisible(false);
				contacts.setVisible(true);
				previousBtn.enable();
				nextBtn.disable();
				createCompanyBtn.enable();
			}
		});
		buttonBar.add(nextBtn);
		companyButtonBinding.addButton(nextBtn);
		
		createCompanyBtn.disable();
		buttonBar.add(createCompanyBtn);
		buttonBar.add(getCancelButton());
		
		this.add(buttonBar);
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
		companynameFld.setAutoValidate(true);
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
		phoneFld.setAutoValidate(true);
		formPanel.add(phoneFld);

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

		return formPanel;
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

	private Button cancelBtn;

	private Button createBtn;
	public void addChangeListener(ChangeListener... listener) {
		changeEventSupport.addChangeListener(listener);
	}
	
	public void removeChangeListener(ChangeListener... listener) {
		changeEventSupport.removeChangeListener(listener);
	}

	private Button getCreateCompanyButton() {
		createBtn = new Button("Opret firma",
				new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (contactStore.getCount() > 0)
					createCompany();
				else {
					Dialog confirmDialog = new Dialog();
					confirmDialog.setButtons(Dialog.YESNO);
					confirmDialog.setHideOnButtonClick(true);
					
					confirmDialog.setHeading("Ønsker du at oprette som kundeemne?");
					confirmDialog.addText("Vil du oprette en virksomhed uden kontakter.");
					confirmDialog.addText("En virksomhed uden kontakter vil dukke op som" +
							"et kundeemne.");
					
					confirmDialog.getButtonById(Dialog.NO).setText("Fortryd");
					confirmDialog.getButtonById(Dialog.YES).setText("Opret kundeemne");
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
		createBtn.setIcon(IconHelper.createPath("images/add.gif"));
		return createBtn;
	}
	
	private Button getCancelButton() {
		cancelBtn = new Button("Anuller", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				fireEvent(Events.Close);
			}
		});
		cancelBtn.setIcon(IconHelper.createPath("images/cancel.gif"));
		
		return cancelBtn; 
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
