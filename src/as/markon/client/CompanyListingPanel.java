package as.markon.client;

import java.util.ArrayList;
import java.util.List;

import as.markon.viewmodel.Company;
import as.markon.viewmodel.Salesman;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.ChangeEvent;
import com.extjs.gxt.ui.client.data.ChangeListener;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CompanyListingPanel extends ContentPanel {
	private LoadingDialog loader = new LoadingDialog();
	private DataServiceAsync dataService = Global.getInstance().getDataService();

	private String checkedStyle = "x-grid3-group-check";
	private String uncheckedStyle = "x-grid3-group-uncheck";
	
	private boolean loadingCustomers = true,
		loadingProspects = true;
	
	private GroupingStore<Company> companyStore;

	private Grid<Company> companyGrid;
	private Grid<Company> prospectGrid;
	private GroupingStore<Company> prospectStore;

	public CompanyListingPanel() {
		checkLoader();
		
		this.setLayout(new AccordionLayout());
		this.setFrame(false);
		this.setBorders(false);
		this.setHeaderVisible(false);
		
		this.add(createCustomerListing());
		this.add(createProspectListing());
	}

	private ToolBar createToolbar(Grid<Company> companyGrid,
			final GroupingStore<Company> storeToAddTo,
			final CheckBoxSelectionModel<Company> sm) {
		ToolBar companyToolBar = new ToolBar();
		
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
				Dialog deleteDialog = new DeleteDialog(sm);
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
		
		return companyToolBar;
	}

	private ContentPanel createCustomerListing() {
		ContentPanel panel = new ContentPanel();
		panel.setLayout(new FitLayout());
		panel.setFrame(false);
		panel.setBorders(false);
		panel.setHeading("Kundeliste");
		
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
		companyGrid.setSelectionModel(sm);
		companyGrid.getView().setEmptyText("Der er ingen virksomheder i listen.");
		
		panel.addListener(Events.Collapse, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				companyGrid.getSelectionModel().deselectAll();
			}
		});
		
		panel.setHeight(550);
		panel.add(companyGrid);
		panel.setTopComponent(createToolbar(companyGrid, companyStore, sm));
		
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

		return new ColumnModel(configs);
	}

	private ContentPanel createProspectListing() {
		ContentPanel panel = new ContentPanel();
		panel.setLayout(new FitLayout());
		panel.setFrame(false);
		panel.setBorders(false);
		panel.setHeading("Kundekandidater");
		
		GroupingView companyView = new CustomGroupingView();
		companyView.setShowGroupedColumn(false);
		companyView.setForceFit(true);
		
		CheckBoxSelectionModel<Company> sm = new CustomCheckBoxSelectionModel(companyView);
		
		ColumnModel cm = createColumnConfigs(sm);
		companyView.setGroupRenderer(new CustomGridGroupRenderer(cm));
		
		prospectStore = new GroupingStore<Company>();
		
		loadingProspects = true;
		checkLoader();
		dataService.getProspectCompanies(new AsyncCallback<ArrayList<Company>>() {
			public void onSuccess(ArrayList<Company> result) {
				prospectStore.setMonitorChanges(true);
				prospectStore.add(result);
				prospectStore.setDefaultSort("companyname", SortDir.ASC);
				prospectStore.groupBy("trade");
				
				loadingProspects = false;
				checkLoader();
			}
			
			public void onFailure(Throwable caught) {
			}
		});
		
		prospectGrid = new Grid<Company>(prospectStore, cm);
		prospectGrid.setAutoExpandColumn("companyname");
		prospectGrid.setView(companyView);
		prospectGrid.setBorders(false);
		prospectGrid.setColumnLines(true);
		prospectGrid.setColumnReordering(true);
		prospectGrid.setStripeRows(true);
		prospectGrid.addPlugin(sm);
		prospectGrid.setSelectionModel(sm);

		panel.addListener(Events.Collapse, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				prospectGrid.getSelectionModel().deselectAll();
			}
		});
		
		panel.setHeight(550);
		panel.add(prospectGrid);
		
		return panel;
	}
	
	public void salesmanChanged() {
		Salesman salesman = Global.getInstance().getCurrentSalesman();
		
		loadingCustomers = true;
		checkLoader();
		
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
	
	private void checkLoader() {
		if (loadingCustomers || loadingProspects)
			loader.show();
		else
			loader.hide();
	}

	private class DeleteDialog extends Dialog {
		public DeleteDialog(final CheckBoxSelectionModel<Company> sm) {
			this.setTitle("Er du sikker på, at du vil slette?");
			this.addText("Advarsel! Du kan ikke fortryde denne handling!"); 
			this.addText("Er du sikker på, at du vil slette de markerede virksomheder?");
			this.setButtons(Dialog.YESNO);
			this.getButtonById(Dialog.YES).setText("Slet virksomheder");
			this.getButtonById(Dialog.NO).setText("Fortryd");
			this.setHideOnButtonClick(true);
			
			this.getButtonById(Dialog.YES).addSelectionListener(
				new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						 dataService.deleteCompanies(sm.getSelectedItems(),
								 new AsyncCallback<Void>() {
									public void onSuccess(Void result) {
										for (Company company : sm.getSelectedItems())
											companyStore.remove(company);
									}
									
									public void onFailure(Throwable caught) {
										// TODO show errormessage right now
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
