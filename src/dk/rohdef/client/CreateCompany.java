package dk.rohdef.client;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ChangeEvent;
import com.extjs.gxt.ui.client.data.ChangeEventSource;
import com.extjs.gxt.ui.client.data.ChangeEventSupport;
import com.extjs.gxt.ui.client.data.ChangeListener;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.DelayedTask;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
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
	private boolean existingCompany = false;
	
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
		
		final LayoutContainer companyArea = new LayoutContainer();
		companyArea.setAutoWidth(true);
		companyArea.setHeight(320);
		companyArea.setLayout(new HBoxLayout());
		this.add(companyArea);
		
		final CompanyEditPanel newCompanyPanel = createNewCompanyPanel();
		final SearchGrid searchResultArea = new SearchGrid();
		searchResultArea.setHeight(300);
		
		final DelayedTask companyNameTask = new DelayedTask(
			new SearchListener(newCompanyPanel,
					searchResultArea));
				newCompanyPanel.addCompanyNameFieldKeyListener(new Listener<FieldEvent>() {
					public void handleEvent(FieldEvent be) {
						companyNameTask.delay(500);
					}
				});
		
		final LayoutContainer contactsArea = new LayoutContainer();
		contactsArea.setAutoWidth(true);
		contactsArea.setHeight(470);
		contactsArea.setLayout(new HBoxLayout());
		contactsArea.setVisible(false);
		this.add(contactsArea);
		
		CreateContactPanel createNewContactPanel = new CreateContactPanel();
		createNewContactPanel.addNewContactListener(new ContactListener() {
			public void handleEvent(ContactEvent be) {
				contactStore.add(be.getContact());
			}
		});
		
		contactsArea.add(createNewContactPanel, new HBoxLayoutData());
		contactsArea.add(getContactList(), new HBoxLayoutData());
		
		companyArea.add(newCompanyPanel);
		companyArea.add(searchResultArea);
		
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
				contactsArea.setVisible(false);
				companyArea.setVisible(true);
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
				companyArea.setVisible(false);
				contactsArea.setVisible(true);
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

	private CompanyEditPanel createNewCompanyPanel() {
		final CompanyEditPanel formPanel = new CompanyEditPanel();
		formPanel.setAutoHeight(true);
		formPanel.setWidth("50%");
		formPanel.setHeading(i18n.insertCompanyDetails());
		
		formPanel.bindCompany(newCompany);

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
		configs.add(new ColumnConfig("contactname", i18n.contactName(), 125));
		configs.add(new ColumnConfig("title", i18n.contactTitle(), 75));
		
		ColumnModel cm = new ColumnModel(configs);
		
		contactGrid = new Grid<Contact>(contactStore, cm);
		contactGrid.setBorders(false);
		contactGrid.setStripeRows(true);
		contactGrid.setHeight(120);
		
		contactGrid.getView().setEmptyText(i18n.noContacts());
		
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
		createBtn = new Button(i18n.createCompany(),
				new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				if (contactStore.getCount() > 0)
					createCompany();
				else {
					Dialog confirmDialog = new Dialog();
					confirmDialog.setButtons(Dialog.YESNO);
					confirmDialog.setHideOnButtonClick(true);
					
					confirmDialog.setHeading(i18n.createProspectTitle());
					confirmDialog.addText(i18n.noContactsQuestion());
					confirmDialog.addText(i18n.noContactCompanyIsProspect());
					
					confirmDialog.getButtonById(Dialog.NO).setText(i18n.cancel());
					confirmDialog.getButtonById(Dialog.YES).setText(i18n.createProspect());
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
		cancelBtn = new Button(i18n.cancel(), new SelectionListener<ButtonEvent>() {
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
					noTrade.setTrade(i18n.noTradeSelected());
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
	
	private class SearchListener implements Listener<BaseEvent> {
		private CompanyEditPanel formPanel;
		private SearchGrid searchResultArea;
		
		public SearchListener(CompanyEditPanel formPanel, SearchGrid searchResultArea) {
			this.formPanel = formPanel;
			this.searchResultArea = searchResultArea;
		}
		
		public void handleEvent(BaseEvent be) {
			if (formPanel.getCompanyName() == null ||
					formPanel.getCompanyName().length() < 3) {
				searchResultArea.setCompanyStore(new GroupingStore<Company>());
			} else {
				dataService.searchForCompany(formPanel.getCompanyName(),
					new AsyncCallback<ArrayList<Company>>() {
						public void onSuccess(ArrayList<Company> result) {
							GroupingStore<Company> store = new GroupingStore<Company>();
							store.setMonitorChanges(true);
							store.add(result);
							searchResultArea.setCompanyStore(store);
						}
		
						public void onFailure(Throwable caught) {
						}
				});
			}
		}
		
	}
	
	private class SearchGrid extends ContentPanel {
		private CustomerCareI18n i18n;
		private Grid<Company> companyGrid;
		private ListStore<Company> companyStore;
		
		public SearchGrid() {
			i18n = Global.getInstance().getI18n();
			
			this.setLayout(new FitLayout());
			this.setHeading(i18n.searchResults());

			ColumnModel cm = createColumnConfigs();
			
			companyStore = new ListStore<Company>();
			companyGrid = new Grid<Company>(companyStore, cm);
			companyGrid.setBorders(false);
			companyGrid.setColumnLines(true);
			companyGrid.setColumnReordering(true);
			companyGrid.setLoadMask(true);
			companyGrid.setStripeRows(true);
			companyGrid.getView().setEmptyText(i18n.emptyCompanyList());
			
			this.addListener(Events.Collapse, new Listener<BaseEvent>() {
				public void handleEvent(BaseEvent be) {
					companyGrid.getSelectionModel().deselectAll();
				}
			});
			
			this.add(companyGrid);
		}
		
		public void setCompanyStore(GroupingStore<Company> store) {
			companyGrid.reconfigure(store, companyGrid.getColumnModel());
			this.companyStore = store;
		}
		
		/**
		 * Create a set of column configs for the company grid. The selection model is to 
		 * get the checkboxes into the grid too.
		 * @param sm
		 * @return
		 */
		private ColumnModel createColumnConfigs() {
			List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

			ColumnConfig column = null;
			column = new ColumnConfig("companyname", i18n.companyName(), 120);
			configs.add(column);

			column = new ColumnConfig("city", i18n.city(), 65);
			configs.add(column);
			
			column = new ColumnConfig("phone", i18n.phone(), 50);
			configs.add(column);
			
			column = new ColumnConfig();
			column.setHeader("Vælg");
			column.setRenderer(new GridCellRenderer<Company>() {
				public Object render(Company model, String property,
						ColumnData config, int rowIndex, int colIndex,
						ListStore<Company> store, Grid<Company> grid) {
					return new Button("Vælg");
				}
			});
			
			column.setWidth(70);
			configs.add(column);

			return new ColumnModel(configs);
		}
	}
}
