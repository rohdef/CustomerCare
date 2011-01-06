package dk.rohdef.client.panels;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

import dk.rohdef.client.LoadingDialog;
import dk.rohdef.client.services.DataServiceAsync;
import dk.rohdef.client.services.Global;
import dk.rohdef.viewmodel.Company;
import dk.rohdef.viewmodel.Contact;
import dk.rohdef.viewmodel.Salesman;

/**
 * Panel for listing both companies for the salesman and prospect companies (it is in an 
 * accordion layout, look in the bottom for prospects).
 * @author Rohde Fischer <rohdef@rohdef.dk>
 */
public class CompanyListingPanel extends ContentPanel {
	private static Logger logger = Logger.getLogger(CompanyListingPanel.class.getName());
	private LoadingDialog loader = new LoadingDialog();
	private DataServiceAsync dataService = Global.getInstance().getDataService();
	
	private boolean loadingCustomers = true,
		loadingProspects = false;

	private CompanyGridPanel companyPanel;
	private CompanyGridPanel prospectPanel;
	private CompanyGridPanel allCompaniesPanel;

	/**
	 * 
	 */
	public CompanyListingPanel() {
		checkLoader();
		
		this.setLayout(new AccordionLayout());
		this.setFrame(false);
		this.setBorders(false);
		this.setHeaderVisible(false);
		
		this.add(createCustomerListing());
		this.add(createProspectListing());
		this.add(createAllCompaniesListing());
	}
	
	/**
	 * 
	 * @return true if currently showing the prospects
	 */
	public boolean isShowingProspects() {
		return prospectPanel.isExpanded();
	}

 	
 	/**
 	 * Get the panel containing the list of companies related to the selected salesman.
 	 * @return
 	 */
	private ContentPanel createCustomerListing() {
		companyPanel = new CompanyGridPanel(false);
		companyPanel.setHeading("Kundeliste");
				
		return companyPanel;
	}

	/**
	 * Get the panel containing the list of prospects
	 * @return
	 */
	private ContentPanel createProspectListing() {
		prospectPanel = new CompanyGridPanel(true);
		prospectPanel.setHeading("Kundeemner");
		
		prospectPanel.addListener(Events.Expand, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				loadingProspects = true;
				checkLoader();
				
				dataService.getProspectCompanies(new AsyncCallback<ArrayList<Company>>() {
					public void onSuccess(ArrayList<Company> result) {
						GroupingStore<Company> store = new GroupingStore<Company>();
						store.setMonitorChanges(true);
						store.add(result);
						store.setDefaultSort("companyname", SortDir.ASC);
						store.groupBy("trade");
						prospectPanel.setCompanyStore(store);
						
						loadingProspects = false;
						checkLoader();
					}
					
					public void onFailure(Throwable caught) {
						logger.log(Level.SEVERE, "Couldn't load prospects", caught);
					}
				});
			}
		});
		
		return prospectPanel;
	}
	
	/**
	 * Get the panel containing the list of prospects
	 * @return
	 */
	private ContentPanel createAllCompaniesListing() {
		allCompaniesPanel = new CompanyGridPanel(true);
		allCompaniesPanel.setHeading("Alle virksomheder");
		
		allCompaniesPanel.addListener(Events.Expand, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				loadingProspects = true;
				checkLoader();
				
				dataService.getAllCompanies(new AsyncCallback<ArrayList<Company>>() {
					public void onSuccess(ArrayList<Company> result) {
						GroupingStore<Company> store = new GroupingStore<Company>();
						store.setMonitorChanges(true);
						store.add(result);
						store.setDefaultSort("companyname", SortDir.ASC);
						store.groupBy("trade");
						allCompaniesPanel.setCompanyStore(store);
						
						loadingProspects = false;
						checkLoader();
					}
					
					public void onFailure(Throwable caught) {
						logger.log(Level.SEVERE, "Couldn't load prospects", caught);
					}
				});
			}
		});
		
		return allCompaniesPanel;
	}
	
	/**
	 * Recieve signal that the salesman has changed. Ensures that the grid is updated 
	 * to account for changes in the salesman.
	 */
	public void salesmanChanged() {
		Salesman salesman = Global.getInstance().getCurrentSalesman();
		
		loadingCustomers = true;
		checkLoader();
		
		logger.info("Getting customer listing");
		dataService.getCompanies(salesman,
				new AsyncCallback<ArrayList<Company>>() {
					public void onSuccess(ArrayList<Company> result) {
						logger.log(Level.FINE, "Successfully got companies");
						GroupingStore<Company> companyStore = new GroupingStore<Company>();
						companyStore.setMonitorChanges(true);
						companyStore.add(result);
						companyStore.setDefaultSort("companyname", SortDir.ASC);
						companyStore.groupBy("trade");
						
						if (companyPanel != null)
							companyPanel.setCompanyStore(companyStore);
						
						logger.log(Level.FINER, "Configured company store");
						
						loadingCustomers = false;
						checkLoader();
					}

					public void onFailure(Throwable caught) {
						logger.log(Level.SEVERE, "Failed to get companies", caught);
						throw new RuntimeException(caught);
					}
				});
	}
	
	/**
	 * Add a selectionlistener to react on selection changes in the company lists.
	 * @param listener
	 */
	public void addSelectionListener(Listener<SelectionChangedEvent<Company>> listener) {
		companyPanel.addSelectionListener(listener);
		prospectPanel.addSelectionListener(listener);
	}
	
	/**
	 * Deselect company from the customers grid.
	 * @param company
	 */
	public void deselectCompany(Company company) {
		if (isShowingProspects()) {
			prospectPanel.deselectCompany(company);
		} else {
			companyPanel.deselectCompany(company);
		}
	}

	/**
	 * Move company from prospects to customers.
	 * @param company
	 */
	public void moveCompanyToCustomers(Company company) {
		companyPanel.addCompany(company);
		if (isShowingProspects())
			prospectPanel.removeCompany(company);
	}
	
	/**
	 * Move the contact from prospects to customers (technically attach the contact to 
	 * a salesman).
	 * @param contact
	 */
	public void moveContactToCustomers(Contact contact) {
		dataService.getCompanyFor(contact, new AsyncCallback<Company>() {
			public void onSuccess(Company result) {
				if (result == null)
					return;
				
				moveCompanyToCustomers(result);
			}
			
			public void onFailure(Throwable caught) {
			}
		});
	}

	/**
	 * Move contact to prospects, eg. if the salesman drops the contact.
	 * @param contact
	 */
	public void moveContactToProspects(Contact contact) {
		dataService.getCompanyFor(contact, new AsyncCallback<Company>() {
			public void onSuccess(final Company company) {
				if (company == null)
					return;
				dataService.getAllContacts(company, new AsyncCallback<ArrayList<Contact>>() {
					public void onSuccess(ArrayList<Contact> result) {
						for (Contact c : result) {
							if (c.getSalesman() != null)
								return;
						}
						
						companyPanel.removeCompany(company);
						if (isShowingProspects()) {
							prospectPanel.addCompany(company);
						}
					}
					
					public void onFailure(Throwable caught) {
					}
				});
			}
			
			public void onFailure(Throwable caught) {
			}
		});
	}
	
	/**
	 * Remove company from the lists (used when the company is deleted).
	 * @param company
	 */
	public void removeCompanyFromLists(Company company) {
		logger.info("Removing " + company.getCompanyName() + " from the list");
		
		companyPanel.removeCompany(company);
		if (isShowingProspects()) {
			prospectPanel.removeCompany(company);
		}
	}
	
	/**
	 * Remove the contact from the lists (when the contact is deleted or moved to another 
	 * salesman than the active one.
	 * @param contact
	 */
	public void removeContactFromLists(Contact contact) {
		logger.info("Removing " + contact.getName() + " from the list");
		
		dataService.getCompanyFor(contact, new AsyncCallback<Company>() {
			public void onSuccess(Company result) {
				if (result == null)
					return;
				
				removeCompanyFromLists(result);
			}
			
			public void onFailure(Throwable caught) {
				logger.log(Level.WARNING, "Coundn't find the right company", caught);
			}
		});
	}
	
	/**
	 * Check if any data is loading to show or hide the loading dialog appropriately.
	 */
	private void checkLoader() {
		if (loadingCustomers || loadingProspects)
			loader.show();
		else
			loader.hide();
	}
}
