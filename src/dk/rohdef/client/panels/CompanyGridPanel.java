package dk.rohdef.client.panels;

import java.util.ArrayList;
import java.util.List;


import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.ChangeEvent;
import com.extjs.gxt.ui.client.data.ChangeListener;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.grid.filters.GridFilters;
import com.extjs.gxt.ui.client.widget.grid.filters.StringFilter;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

import dk.rohdef.client.services.DataServiceAsync;
import dk.rohdef.client.services.Global;
import dk.rohdef.viewmodel.Company;

public class CompanyGridPanel extends ContentPanel {
	public enum CompanyGridType { ALL, PROSPECTS, CUSTOMERS, };
	
	private DataServiceAsync dataService;
	private Grid<Company> companyGrid;
	private GroupingStore<Company> companyStore;
	
	private String checkedStyle = "x-grid3-group-check";
	private String uncheckedStyle = "x-grid3-group-uncheck";
	
	private CompanyGridType companyGridType;

	public CompanyGridPanel(CompanyGridType companyGridType) {
		dataService = Global.getInstance().getDataService();
		this.companyGridType = companyGridType;
		
		this.setLayout(new FitLayout());
		this.setFrame(false);
		this.setBorders(false);

		GroupingView companyView = new CustomGroupingView();
		companyView.setShowGroupedColumn(false);
		companyView.setForceFit(true);
		CheckBoxSelectionModel<Company> sm = new CustomCheckBoxSelectionModel(companyView);
		
		ColumnModel cm = createColumnConfigs(sm);
		companyView.setGroupRenderer(new CustomGridGroupRenderer(cm));
		
		GridFilters filters = new GridFilters();
		filters.setLocal(true);
		StringFilter companyNameFilter = new StringFilter("companyname");
		filters.addFilter(companyNameFilter);
		
		companyStore = new GroupingStore<Company>();
		companyGrid = new Grid<Company>(companyStore, cm);
		companyGrid.setAutoExpandColumn("companyname");
		companyGrid.setBorders(false);
		companyGrid.setColumnLines(true);
		companyGrid.setColumnReordering(true);
		companyGrid.setLoadMask(true);
		companyGrid.setSelectionModel(sm);
		companyGrid.setStripeRows(true);
		companyGrid.setView(companyView);
		companyGrid.addPlugin(sm);
		companyGrid.addPlugin(filters);
		companyGrid.getView().setEmptyText("Der er ingen virksomheder i listen.");
		
		this.addListener(Events.Collapse, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				companyGrid.getSelectionModel().deselectAll();
			}
		});
		
		this.setHeight(550);
		this.add(companyGrid);
		this.setTopComponent(initializeToolBar(companyNameFilter));
	}
	
	public void setCompanyStore(GroupingStore<Company> store) {
		companyGrid.reconfigure(store, companyGrid.getColumnModel());
		this.companyStore = store;
	}
	
	public void addCompany(Company company) {
		companyStore.add(company);
	}
	
	public void removeCompany(Company company) {
		companyStore.remove(company);
	}
	
	public void addSelectionListener(Listener<SelectionChangedEvent<Company>> listener) {
		companyGrid.getSelectionModel().addListener(Events.SelectionChange, listener);
	}
	
	public void removeSelectionListener(Listener<SelectionChangedEvent<Company>> listener) {
		companyGrid.getSelectionModel().removeListener(Events.SelectionChange, listener);
	}
	
	public void deselectCompany(Company company) {
		companyGrid.getSelectionModel().deselect(company);
	}
	
	private ToolBar initializeToolBar(StringFilter filter) {
		final GridSelectionModel<Company> sm = companyGrid.getSelectionModel();
		
		final CompanyGridToolBar toolBar = new CompanyGridToolBar(filter, 
				(companyGridType != CompanyGridType.CUSTOMERS));
		
		toolBar.addCreateCompanyListener(new ChangeListener() {
			public void modelChanged(ChangeEvent event) {
				Company company = (Company) event.getSource();
				if ((Boolean)company.get("prospect") == (companyGridType == CompanyGridType.PROSPECTS)
						|| companyGridType == CompanyGridType.ALL)
					companyStore.add(company);
			}
		});
		
		toolBar.addDeleteCompanyListener(new SelectionListener<ComponentEvent>() {
			@Override
			public void componentSelected(ComponentEvent ce) {
				dataService.deleteCompanies(sm.getSelectedItems(),
						 new AsyncCallback<Void>() {
							public void onSuccess(Void result) {
								for (Company company : sm.getSelectedItems())
									companyStore.remove(company);
							}
							
							public void onFailure(Throwable caught) {
								throw new RuntimeException(caught);
							}
						});
			}
		});
		
		companyGrid.getSelectionModel().addListener(Events.SelectionChange,
				new Listener<SelectionChangedEvent<Company>>() {
					public void handleEvent(SelectionChangedEvent<Company> be) {
						toolBar.setCompaniesSelected(be.getSelection().size());
						
					}
			});
		
		return toolBar;
	}
	
	/**
	 * Create a set of column configs for the company grid. The selection model is to 
	 * get the checkboxes into the grid too.
	 * @param sm
	 * @return
	 */
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
