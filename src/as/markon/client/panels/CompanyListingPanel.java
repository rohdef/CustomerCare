package as.markon.client.panels;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import as.markon.client.CreateCompany;
import as.markon.client.LoadingDialog;
import as.markon.client.SalesmanAdminWindow;
import as.markon.client.TradeAdminDialog;
import as.markon.client.services.DataServiceAsync;
import as.markon.client.services.Global;
import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;
import as.markon.viewmodel.Salesman;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.ChangeEvent;
import com.extjs.gxt.ui.client.data.ChangeListener;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.grid.filters.GridFilters;
import com.extjs.gxt.ui.client.widget.grid.filters.StringFilter;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CompanyListingPanel extends ContentPanel {
	private static Logger logger = Logger.getLogger(CompanyListingPanel.class.getName());
	private LoadingDialog loader = new LoadingDialog();
	private DataServiceAsync dataService = Global.getInstance().getDataService();

	private String checkedStyle = "x-grid3-group-check";
	private String uncheckedStyle = "x-grid3-group-uncheck";
	
	private boolean loadingCustomers = true,
		loadingProspects = false;
	

	private Grid<Company> companyGrid;
	private Grid<Company> prospectGrid;
	private GroupingStore<Company> companyStore;
	private GroupingStore<Company> prospectStore;
	private ContentPanel prospectListing;

	public CompanyListingPanel() {
		checkLoader();
		
		this.setLayout(new AccordionLayout());
		this.setFrame(false);
		this.setBorders(false);
		this.setHeaderVisible(false);
		
		this.add(createCustomerListing());
		prospectListing = createProspectListing();
		this.add(prospectListing);
		
		salesmanChanged();
	}
	
	public boolean isShowingProspects() {
		return prospectListing.isExpanded();
	}

 	private ToolBar createToolbar(final Grid<Company> companyGrid,
			final GroupingStore<Company> currentStore,
			final CheckBoxSelectionModel<Company> sm,
			final StringFilter filter,
			final boolean prospect) {
		ToolBar companyToolBar = new ToolBar();
		companyToolBar.setEnableOverflow(false);
		
		companyToolBar.add(new LabelToolItem("Søg: "));
		
		final TextField<String> searchFld = new TextField<String>();
		searchFld.setWidth(100);
		searchFld.addKeyListener(new KeyListener() {
			@Override
			public void handleEvent(ComponentEvent e) {
				super.handleEvent(e);
				filter.setValue(searchFld.getValue());
			}
		});
		companyToolBar.add(searchFld);
		
		Button newCompany = new Button();
		newCompany.setText("Opret ny virksomhed");
		newCompany.setIcon(IconHelper.createPath("images/add.gif"));
		newCompany.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				final Window createCompanyWindow = new Window();
				final CreateCompany createCompany = new CreateCompany();
				createCompany.addListener(Events.Close, new Listener<BaseEvent>() {
					public void handleEvent(BaseEvent be) {
						createCompanyWindow.hide();
					}
				});
				
				createCompany.addChangeListener(new ChangeListener() {
					public void modelChanged(ChangeEvent event) {
						Company company = (Company) event.getSource();
						if ((Boolean)company.get("prospect"))
							prospectStore.add(company);
						else
							companyStore.add(company);
					}
					
				});
				
				createCompanyWindow.add(createCompany);
				createCompanyWindow.setSize(650, 475);
				createCompanyWindow.setModal(true);
				createCompanyWindow.setHeading("Opret ny virksomhed");
				createCompanyWindow.setLayout(new FitLayout());
				createCompanyWindow.show();
			}
		});
		companyToolBar.add(newCompany);
		
		final Button deleteCompaniesBtn = new Button();
		deleteCompaniesBtn.setText("Slet markerede firmaer");
		deleteCompaniesBtn.disable();
		deleteCompaniesBtn.setIcon(IconHelper.createPath("images/delete.gif"));
		deleteCompaniesBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				Dialog deleteDialog = new DeleteDialog(sm, prospect);
				deleteDialog.show();
			}
		});
		
		companyGrid.getSelectionModel().addListener(Events.SelectionChange,
				new Listener<SelectionChangedEvent<Company>>() {
					public void handleEvent(SelectionChangedEvent<Company> be) {
						if (be.getSelection().size() == 1 )
							deleteCompaniesBtn.enable();
						else
							deleteCompaniesBtn.disable();
					}
			});
		
		companyToolBar.add(deleteCompaniesBtn);
		
		if (!prospect) {
			final Button tradeAdminBtn = new Button();
			tradeAdminBtn.setText("Administrer brancher");
			tradeAdminBtn.setIcon(IconHelper.createPath("images/trades.gif"));
			tradeAdminBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					TradeAdminDialog dialog = new TradeAdminDialog();
					dialog.show();
				}
			});
			companyToolBar.add(tradeAdminBtn);
			
			final Button salesmanAdminBtn = new Button();
			salesmanAdminBtn.setText("Administrer sælgere");
			salesmanAdminBtn.setIcon(IconHelper.createPath("images/salesmen.gif"));
			salesmanAdminBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					SalesmanAdminWindow window = new SalesmanAdminWindow();
					window.show();
				}
			});
			companyToolBar.add(salesmanAdminBtn);
		}
		
		return companyToolBar;
	}
 	
	private ContentPanel createCustomerListing() {
		ContentPanel panel = new ContentPanel();
		panel.setLayout(new FitLayout());
		panel.setFrame(false);
		panel.setBorders(false);
		panel.setHeading("Kundeliste");
		
		GridFilters filters = new GridFilters();
		filters.setLocal(true);
		
		StringFilter companyNameFilter = new StringFilter("companyname");
		filters.addFilter(companyNameFilter);
		
		GroupingView companyView = new CustomGroupingView();
		companyView.setShowGroupedColumn(false);
		companyView.setForceFit(true);
		
		CheckBoxSelectionModel<Company> sm = new CustomCheckBoxSelectionModel(companyView);
		
		ColumnModel cm = createColumnConfigs(sm);
		companyView.setGroupRenderer(new CustomGridGroupRenderer(cm));
		
		companyStore = new GroupingStore<Company>();

		companyGrid = new Grid<Company>(companyStore, cm);
		companyGrid.setAutoExpandColumn("companyname");
		companyGrid.setView(companyView);
		companyGrid.setBorders(false);
		companyGrid.setColumnLines(true);
		companyGrid.setColumnReordering(true);
		companyGrid.setLoadMask(true);
		companyGrid.setStripeRows(true);
		companyGrid.addPlugin(sm);
		companyGrid.addPlugin(filters);
		companyGrid.setSelectionModel(sm);
		companyGrid.getView().setEmptyText("Der er ingen virksomheder i listen.");
		
		panel.addListener(Events.Collapse, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				companyGrid.getSelectionModel().deselectAll();
			}
		});
		
		panel.setHeight(550);
		panel.add(companyGrid);
		panel.setTopComponent(createToolbar(companyGrid, companyStore, sm,
				companyNameFilter, false));
		
		return panel;
	}

	private ColumnModel createColumnConfigs(CheckBoxSelectionModel<Company> sm) {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		configs.add(sm.getColumn());

		ColumnConfig column = new ColumnConfig("companyname", "Firmanavn", 200);
		configs.add(column);

		column = new ColumnConfig("postal", "Postnr", 35);
		configs.add(column);

		column = new ColumnConfig("city", "By", 125);
		configs.add(column);
		
		column = new ColumnConfig("trade", "Branche", 125);
		configs.add(column);
		
		column = new ColumnConfig("phone", "Telefon", 50);
		configs.add(column);

		return new ColumnModel(configs);
	}

	private ContentPanel createProspectListing() {
		ContentPanel panel = new ContentPanel();
		panel.setLayout(new FitLayout());
		panel.setFrame(false);
		panel.setBorders(false);
		panel.setHeading("Kundekandidater");
		
		GridFilters filters = new GridFilters();
		filters.setLocal(true);
		
		StringFilter companyNameFilter = new StringFilter("companyname");
		filters.addFilter(companyNameFilter);
		
		GroupingView companyView = new CustomGroupingView();
		companyView.setShowGroupedColumn(false);
		companyView.setForceFit(true);
		
		CheckBoxSelectionModel<Company> sm = new CustomCheckBoxSelectionModel(companyView);
		
		final ColumnModel cm = createColumnConfigs(sm);
		companyView.setGroupRenderer(new CustomGridGroupRenderer(cm));
		
		prospectStore = new GroupingStore<Company>();
		
		prospectGrid = new Grid<Company>(prospectStore, cm);
		prospectGrid.setAutoExpandColumn("companyname");
		prospectGrid.setView(companyView);
		prospectGrid.setBorders(false);
		prospectGrid.setColumnLines(true);
		prospectGrid.setColumnReordering(true);
		prospectGrid.setStripeRows(true);
		prospectGrid.addPlugin(sm);
		prospectGrid.addPlugin(filters);
		prospectGrid.setSelectionModel(sm);

		panel.addListener(Events.Collapse, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				prospectGrid.getSelectionModel().deselectAll();
			}
		});
		
		panel.setHeight(550);
		panel.add(prospectGrid);
		panel.setTopComponent(createToolbar(prospectGrid, prospectStore, sm,
				companyNameFilter, true));
		
		panel.addListener(Events.Expand, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				loadingProspects = true;
				checkLoader();
				
				dataService.getProspectCompanies(new AsyncCallback<ArrayList<Company>>() {
					public void onSuccess(ArrayList<Company> result) {
						prospectStore = new GroupingStore<Company>();
						prospectStore.setMonitorChanges(true);
						prospectStore.add(result);
						prospectStore.setDefaultSort("companyname", SortDir.ASC);
						prospectStore.groupBy("trade");
						prospectGrid.reconfigure(prospectStore, cm);
						
						loadingProspects = false;
						checkLoader();
					}
					
					public void onFailure(Throwable caught) {
						logger.log(Level.SEVERE, "Couldn't load prospects", caught);
					}
				});
			}
		});
		
		return panel;
	}
	
	public void salesmanChanged() {
		Salesman salesman = Global.getInstance().getCurrentSalesman();
		
		loadingCustomers = true;
		checkLoader();
		
		logger.info("Getting customer listing");
		dataService.getCompanies(salesman,
				new AsyncCallback<ArrayList<Company>>() {
					public void onSuccess(ArrayList<Company> result) {
						companyStore = new GroupingStore<Company>();
						companyStore.setMonitorChanges(true);
						companyStore.add(result);
						companyStore.setDefaultSort("companyname", SortDir.ASC);
						companyStore.groupBy("trade");
						
						if (companyGrid != null)
							companyGrid.reconfigure(companyStore,
									companyGrid.getColumnModel());
						
						loadingCustomers = false;
						checkLoader();
					}

					public void onFailure(Throwable caught) {
						caught.printStackTrace();
						throw new RuntimeException(caught);
					}
				});
	}
	
	public void addSelectionListener(Listener<SelectionChangedEvent<Company>> listener) {
		companyGrid.getSelectionModel().addListener(Events.SelectionChange, listener);
		prospectGrid.getSelectionModel().addListener(Events.SelectionChange, listener);
	}
	
	public void deselectCompany(Company company) {
		companyGrid.getSelectionModel().deselect(company);
	}

	public void moveContactToCustomers(Contact contact) {
		dataService.getCompanyFor(contact, new AsyncCallback<Company>() {
			public void onSuccess(Company result) {
				if (result == null)
					return;
				
				companyStore.add(result);
				if (isShowingProspects())
					prospectStore.remove(result);
			}
			
			public void onFailure(Throwable caught) {
			}
		});
		
	}
	
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
						
						companyStore.remove(company);	
						if (isShowingProspects()) {
							prospectStore.add(company);
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
	
	public void removeContactFromLists(Contact contact) {
		dataService.getCompanyFor(contact, new AsyncCallback<Company>() {
			public void onSuccess(Company result) {
				if (result == null)
					return;
				
				companyStore.remove(result);
				if (isShowingProspects()) {
					prospectStore.remove(result);
				}
			}
			
			public void onFailure(Throwable caught) {
			}
		});
	}
	
	private void checkLoader() {
		if (loadingCustomers || loadingProspects)
			loader.show();
		else
			loader.hide();
	}

	private class DeleteDialog extends Dialog {
		public DeleteDialog(final CheckBoxSelectionModel<Company> sm,
				final boolean prospects) {
			this.setTitle("Er du sikker på, at du vil slette?");
			this.addText("Advarsel! Du kan ikke fortryde denne handling!"); 
			this.addText("Er du sikker på, at du vil slette den markerede virksomhed?");
			this.setButtons(Dialog.YESNO);
			this.getButtonById(Dialog.YES).setText("Slet virksomhed");
			this.getButtonById(Dialog.NO).setText("Fortryd");
			this.setHideOnButtonClick(true);
			
			this.getButtonById(Dialog.YES).addSelectionListener(
				new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						 dataService.deleteCompanies(sm.getSelectedItems(),
								 new AsyncCallback<Void>() {
									public void onSuccess(Void result) {
										GroupingStore<Company> dataStore;
										if (prospects)
											dataStore = prospectStore;
										else
											dataStore = companyStore;
										for (Company company : sm.getSelectedItems())
											dataStore.remove(company);
									}
									
									public void onFailure(Throwable caught) {
										throw new RuntimeException(caught);
									}
								});
					}
				});
		}
	}

	private class CustomCheckBoxSelectionModel extends CheckBoxSelectionModel<Company> {
		private GroupingView companyView;
		
		public CustomCheckBoxSelectionModel(GroupingView companyView) {
			this.companyView = companyView;
		}

		@Override
		public void deselectAll() {
			super.deselectAll();
			NodeList<com.google.gwt.dom.client.Element> groups = companyView
					.getGroups();
			for (int i = 0; i < groups.getLength(); i++) {
				com.google.gwt.dom.client.Element group = groups.getItem(i)
						.getFirstChildElement();
				setGroupChecked((Element) group, false);
			}
		}

		@Override
		public void selectAll() {
			super.selectAll();
			NodeList<com.google.gwt.dom.client.Element> groups = companyView
					.getGroups();
			for (int i = 0; i < groups.getLength(); i++) {
				com.google.gwt.dom.client.Element group = groups.getItem(i)
						.getFirstChildElement();
				setGroupChecked((Element) group, true);
			}
		}

		@Override
		protected void doDeselect(List<Company> models, boolean supressEvent) {
			super.doDeselect(models, supressEvent);
			NodeList<com.google.gwt.dom.client.Element> groups = companyView.getGroups();
			search: for (int i = 0; i < groups.getLength(); i++) {
				com.google.gwt.dom.client.Element group = groups.getItem(i);
				NodeList<Element> rows = El.fly(group).select(
						".x-grid3-row");
				for (int j = 0, len = rows.getLength(); j < len; j++) {
					Element r = rows.getItem(j);
					int idx = grid.getView().findRowIndex(r);
					Company m = grid.getStore().getAt(idx);
					if (!isSelected(m)) {
						setGroupChecked((Element) group, false);
						continue search;
					}
				}
			}

		}

		@Override
		protected void doSelect(List<Company> models, boolean keepExisting,
				boolean supressEvent) {
			super.doSelect(models, keepExisting, supressEvent);
			NodeList<com.google.gwt.dom.client.Element> groups = companyView
					.getGroups();
			search: for (int i = 0; i < groups.getLength(); i++) {
				com.google.gwt.dom.client.Element group = groups.getItem(i);
				NodeList<Element> rows = El.fly(group).select(
						".x-grid3-row");
				for (int j = 0, len = rows.getLength(); j < len; j++) {
					Element r = rows.getItem(j);
					int idx = grid.getView().findRowIndex(r);
					Company m = grid.getStore().getAt(idx);
					if (!isSelected(m)) {
						continue search;
					}
				}
				setGroupChecked((Element) group, true);

			}
		}
		
		private El findCheck(Element group) {
			return El.fly(group).selectNode(".x-grid3-group-checker").firstChild();
		}
		
		private void setGroupChecked(Element group, boolean checked) {
			findCheck(group).replaceStyleName(
					checked ? uncheckedStyle : checkedStyle,
					checked ? checkedStyle : uncheckedStyle);
		}
	}

	private class CustomGroupingView extends GroupingView {
		@Override
		protected void onMouseDown(GridEvent<ModelData> ge) {
			El hd = ge.getTarget(".x-grid-group-hd", 10);
			El target = ge.getTargetEl();
			if (hd != null && target.hasStyleName(uncheckedStyle)
					|| target.hasStyleName(checkedStyle)) {
				boolean checked = !ge.getTargetEl().hasStyleName(
						uncheckedStyle);
				checked = !checked; // FIXME WHAT THE HOOTING HELL

				if (checked) {
					ge.getTargetEl().replaceStyleName(uncheckedStyle,
							checkedStyle);
				} else {
					ge.getTargetEl().replaceStyleName(checkedStyle,
							uncheckedStyle);
				}

				Element group = (Element) findGroup(ge.getTarget());
				if (group != null) {
					NodeList<Element> rows = El.fly(group).select(
							".x-grid3-row");
					List<ModelData> temp = new ArrayList<ModelData>();
					for (int i = 0; i < rows.getLength(); i++) {
						Element r = rows.getItem(i);
						int idx = findRowIndex(r);
						ModelData m = grid.getStore().getAt(idx);
						temp.add(m);
					}
					if (checked) {
						grid.getSelectionModel().select(temp, true);
					} else {
						grid.getSelectionModel().deselect(temp);
					}
				}
				return;
			}
			super.onMouseDown(ge);
		}
	}

	private class CustomGridGroupRenderer implements GridGroupRenderer {
		private ColumnModel cm;
		
		public CustomGridGroupRenderer(ColumnModel cm) {
			this.cm = cm;
		}

		public String render(GroupColumnData data) {
			String f = cm.getColumnById(data.field).getHeader();
			String l = data.models.size() == 1 ? "Item" : "Items";
			return "<div class='x-grid3-group-checker'><div class='"
					+ uncheckedStyle + "'> </div></div> " + f + ": "
					+ data.group + " (" + data.models.size() + " " + l
					+ ")";
		}
	}

}
