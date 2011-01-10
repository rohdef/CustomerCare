package dk.rohdef.client;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.data.ChangeEvent;
import com.extjs.gxt.ui.client.data.ChangeEventSource;
import com.extjs.gxt.ui.client.data.ChangeEventSupport;
import com.extjs.gxt.ui.client.data.ChangeListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

import dk.rohdef.client.events.ContactEvent;
import dk.rohdef.client.events.ContactListener;
import dk.rohdef.client.i18n.CustomerCareI18n;
import dk.rohdef.client.panels.CompanyEditPanel;
import dk.rohdef.client.panels.CreateContactPanel;
import dk.rohdef.client.services.DataServiceAsync;
import dk.rohdef.client.services.Global;
import dk.rohdef.viewmodel.Company;
import dk.rohdef.viewmodel.Contact;
import dk.rohdef.viewmodel.Importance;
import dk.rohdef.viewmodel.Trade;

/**
 * Handles company creation, with the two local steps create the new company or find an 
 * existing, then insert the relevant contacts. The actual creation though is deferred 
 * until the user presses create company. 
 * @author Rohde Fischer <rohdef@rohdef.dk>
 */
public class CreateCompany extends LayoutContainer {
	public static EventType CompanyCreated = new EventType();
	
	private DataServiceAsync dataService;
	private CustomerCareI18n i18n;
	private LoadingDialog loader = new LoadingDialog();
	private boolean loadingCities = true, loadingTrades = true;
	
	private Company newCompany;
	private ListStore<Contact> contactStore;
	private Grid<Contact> contactGrid;

	/**
	 * 
	 */
	public CreateCompany() {
		dataService = Global.getInstance().getDataService();
		i18n = Global.getInstance().getI18n();
		
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
		final Button previousBtn = new Button(i18n.previous());
		final Button nextBtn = new Button(i18n.next());
		final Button createCompanyBtn = getCreateCompanyButton();
		ButtonBar buttonBar = new ButtonBar();
		
		previousBtn.disable();
		previousBtn.setIcon(IconHelper.createPath("images/arrow_left.gif"));
		previousBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				companyButtonBinding.addButton(nextBtn);
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
	
	/**
	 * Checks if the system is loading data to determine wether to show the loader or not.
	 */
	private void checkLoader() {
		if (loadingCities || loadingTrades)
			loader.show();
		else
			loader.hide();
	}

	private FormPanel createNewCompanyPanel() {
		final CompanyEditPanel formPanel = new CompanyEditPanel();
		formPanel.setAutoHeight(true);
		formPanel.setWidth("50%");
		formPanel.setHeading("Indtast virksomhedsoplysninger");
		
		formPanel.bindCompany(newCompany);

//		postalBox.addSelectionChangedListener(new SelectionChangedListener<City>() {
//					@Override
//					public void selectionChanged(SelectionChangedEvent<City> se) {
//						newCompany.setPostal(se.getSelectedItem().getPostal());
//						
//						if (cityBox.getSelection().equals(se.getSelection()))
//							return;
//						cityBox.setSelection(se.getSelection());
//					}
//				});

		return formPanel;
	}
	
	/**
	 * Creates a panel to list the added contacts
	 * @return
	 */
	private ContentPanel getContactList() {
		ContentPanel contactsPanel = new ContentPanel();
		contactsPanel.setWidth("50%");
		contactsPanel.setHeading(i18n.contactList());
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
