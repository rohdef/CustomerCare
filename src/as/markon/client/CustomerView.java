package as.markon.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import as.markon.viewmodel.City;
import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;
import as.markon.viewmodel.Importance;
import as.markon.viewmodel.Salesman;
import as.markon.viewmodel.Trade;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CustomerView extends LayoutContainer {
	private GroupingStore<Company> companyStore = new GroupingStore<Company>();
	private DataServiceAsync dataService = GWT.create(DataService.class);
	private Salesman salesman;

	private String checkedStyle = "x-grid3-group-check";
	private String uncheckedStyle = "x-grid3-group-uncheck";
	private GroupingView companyView;

	public Salesman getSalesman() {
		return salesman;
	}

	public void setSalesman(Salesman salesman) {
		this.salesman = salesman;

		dataService.getCompanies(salesman.getSalesmanid(),
				new AsyncCallback<ArrayList<Company>>() {
					public void onSuccess(ArrayList<Company> result) {
						companyStore.removeAll();
						companyStore.setMonitorChanges(true);
						companyStore.add(result);
						companyStore.setDefaultSort("companyname", SortDir.ASC);
						companyStore.groupBy("trade");
					}

					public void onFailure(Throwable caught) {
						krHandleError(caught);
					}
				});
	}

	private ColumnModel cm;
	private Grid<Company> companyGrid;
	private ListStore<City> cityStore;

	public CustomerView(Salesman salesman) {
		setSalesman(salesman);
		BorderLayout layout = new BorderLayout();
		this.setLayout(layout);

		// NORTH
		ContentPanel northPanel = new ContentPanel();
		HBoxLayout topLayout = new HBoxLayout();
		northPanel.setLayout(topLayout);

		Text customersTxt = new Text();
		customersTxt.setText("Kunder");
		customersTxt.setStyleAttribute("font-size", "3em;");
		northPanel.add(customersTxt);

		// TODO Add login link

		// City store
		cityStore = new ListStore<City>();
		dataService.getCities(new AsyncCallback<ArrayList<City>>() {
			public void onSuccess(ArrayList<City> result) {
				cityStore.add(result);
				cityStore.sort("postal", SortDir.ASC);
			}

			public void onFailure(Throwable caught) {
				krHandleError(caught);
			}
		});

		// REST
		ContentPanel centerPanel = createCenterPanel();
		ContentPanel eastPanel = createEastPanel();

		this.add(northPanel, new BorderLayoutData(LayoutRegion.NORTH, 100));
		this.add(centerPanel, new BorderLayoutData(LayoutRegion.CENTER, 0.7f));
		this.add(eastPanel, new BorderLayoutData(LayoutRegion.EAST, 0.3f));

	}

	private ContentPanel createCenterPanel() {
		ContentPanel centerPanel = new ContentPanel();
		FitLayout centerLayout = new FitLayout();
		centerPanel.setLayout(centerLayout);

		centerPanel.setHeading("Virksomheder");

		final CheckBoxSelectionModel<Company> sm = new CheckBoxSelectionModel<Company>() {
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
		};
//		sm.setSelectionMode(SelectionMode.MULTI);

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

		cm = new ColumnModel(configs);

		companyView = new GroupingView() {
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
		};

		companyView.setShowGroupedColumn(false);
		companyView.setForceFit(true);
		companyView.setGroupRenderer(new GridGroupRenderer() {
			public String render(GroupColumnData data) {
				String f = cm.getColumnById(data.field).getHeader();
				String l = data.models.size() == 1 ? "Item" : "Items";
				return "<div class='x-grid3-group-checker'><div class='"
						+ uncheckedStyle + "'> </div></div> " + f + ": "
						+ data.group + " (" + data.models.size() + " " + l
						+ ")";
			}
		});

		companyGrid = new Grid<Company>(companyStore, cm);
		companyGrid.setAutoExpandColumn("companyname");
		companyGrid.setView(companyView);
		companyGrid.setBorders(false);
		companyGrid.setColumnLines(true);
		companyGrid.setColumnReordering(true);
		companyGrid.setStripeRows(true);
		companyGrid.addPlugin(sm);
		companyGrid.setSelectionModel(sm);

		centerPanel.setHeight(550);
		centerPanel.add(companyGrid);
		return centerPanel;
	}

	private El findCheck(Element group) {
		return El.fly(group).selectNode(".x-grid3-group-checker").firstChild();
	}

	private void setGroupChecked(Element group, boolean checked) {
		findCheck(group).replaceStyleName(
				checked ? uncheckedStyle : checkedStyle,
				checked ? checkedStyle : uncheckedStyle);
	}

	private ContentPanel createEastPanel() {
		ContentPanel eastPanel = new ContentPanel();
//		eastPanel.setHeading("Firmadata");
		eastPanel.setHeaderVisible(false);

		final FormPanel companyForm = createEastCompanyForm();
		final FormPanel contactForm = createEastContactsForm();
		final FormPanel mailForm = createEastMailForm();
		
		companyForm.setVisible(true);
		contactForm.setVisible(true);
		mailForm.setVisible(false);
		
		eastPanel.add(companyForm);
		eastPanel.add(contactForm);
		eastPanel.add(mailForm);
		
		companyGrid.getSelectionModel().addListener(Events.SelectionChange,
				new Listener<SelectionChangedEvent<Company>>() {
					public void handleEvent(SelectionChangedEvent<Company> be) {
						if (be.getSelection().size() > 1) {
							companyForm.hide();
							contactForm.hide();
							mailForm.show();
						} else {
							companyForm.show();
							contactForm.show();
							mailForm.hide();
						}
					}
		});

		return eastPanel;
	}

	private FormPanel createEastContactsForm() {
		FormPanel contactsForm = new FormPanel();
		contactsForm.setHeading("Kontakter");

		final ListStore<Contact> emptyStore = new ListStore<Contact>();

		final ComboBox<Contact> contactsBox = new ComboBox<Contact>();
		contactsBox.setFieldLabel("Kontaktliste");
		contactsBox.setDisplayField("contactname");
		contactsBox.setTypeAhead(true);
		contactsBox.setStore(emptyStore);
		contactsBox.setTriggerAction(TriggerAction.ALL);
		contactsForm.add(contactsBox);

		TextField<String> nameFld = new TextField<String>();
		nameFld.setBorders(false);
		nameFld.setFieldLabel("Navn");
		nameFld.setName("contactname");
		contactsForm.add(nameFld);

		TextField<String> titleFld = new TextField<String>();
		titleFld.setBorders(false);
		titleFld.setFieldLabel("Titel");
		titleFld.setName("title");
		contactsForm.add(titleFld);

		TextField<String> phoneFld = new TextField<String>();
		phoneFld.setBorders(false);
		phoneFld.setFieldLabel("Telefon");
		phoneFld.setName("phone");
		contactsForm.add(phoneFld);

		TextField<String> mailFld = new TextField<String>();
		mailFld.setBorders(false);
		mailFld.setFieldLabel("Mail");
		mailFld.setName("mail");
		contactsForm.add(mailFld);

		TextArea commentFld = new TextArea();
		commentFld.setBorders(false);
		commentFld.setFieldLabel("Kommentarer");
		commentFld.setName("comments");
		contactsForm.add(commentFld);

		companyGrid.getSelectionModel().addListener(Events.SelectionChange,
				new Listener<SelectionChangedEvent<Company>>() {
					public void handleEvent(SelectionChangedEvent<Company> be) {
						if (be.getSelection().size() > 0) {
							contactsBox.clear();

							ListStore<Contact> contactStore = new ListStore<Contact>();
							contactStore
									.add(be.getSelectedItem().getContacts());

							contactsBox.setStore(contactStore);
						} else
							contactsBox.setStore(emptyStore);
					}
				});

		final FormBinding contactBinding = new FormBinding(contactsForm, true);

		contactsBox
				.addSelectionChangedListener(new SelectionChangedListener<Contact>() {
					@Override
					public void selectionChanged(
							SelectionChangedEvent<Contact> se) {
						contactBinding.bind(se.getSelectedItem());
					}
				});

		return contactsForm;
	}

	private FormPanel createEastCompanyForm() {
		FormPanel companyForm = new FormPanel();
		companyForm.setHeading("Firmadata");

		TextField<String> addressFld = new TextField<String>();
		addressFld.setBorders(false);
		addressFld.setFieldLabel("Adresse");
		addressFld.setName("address");
		companyForm.add(addressFld);

		final ComboBox<City> postalBox = new ComboBox<City>();
		postalBox.setFieldLabel("Postnummer");
		postalBox.setDisplayField("postal");
		postalBox.setTypeAhead(true);
		postalBox.setStore(cityStore);
		postalBox.setTriggerAction(TriggerAction.ALL);
		companyForm.add(postalBox);

		final ComboBox<City> cityBox = new ComboBox<City>();
		cityBox.setFieldLabel("By");
		cityBox.setDisplayField("cityname");
		cityBox.setTypeAhead(true);
		cityBox.setStore(cityStore);
		cityBox.setTriggerAction(TriggerAction.ALL);
		companyForm.add(cityBox);

		postalBox.addSelectionChangedListener(new SelectionChangedListener<City>() {
					@Override
					public void selectionChanged(SelectionChangedEvent<City> se) {
						cityBox.setSelection(se.getSelection());
					}
				});

		cityBox.addSelectionChangedListener(new SelectionChangedListener<City>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<City> se) {
				postalBox.setSelection(se.getSelection());
			}
		});

		TextField<String> phoneFld = new TextField<String>();
		phoneFld.setBorders(false);
		phoneFld.setFieldLabel("Telefon");
		phoneFld.setName("phone");
		companyForm.add(phoneFld);

		TextField<String> mailFld = new TextField<String>();
		mailFld.setBorders(false);
		mailFld.setFieldLabel("Mail");
		mailFld.setName("mail");
		companyForm.add(mailFld);

		final ListStore<Trade> tradeStore = new ListStore<Trade>();

		dataService.getTrades(new AsyncCallback<ArrayList<Trade>>() {
			public void onSuccess(ArrayList<Trade> result) {
				tradeStore.removeAll();
				tradeStore.add(result);
			}

			public void onFailure(Throwable caught) {
				krHandleError(caught);
			}
		});

		ComboBox<Trade> tradeBox = new ComboBox<Trade>();
		tradeBox.setFieldLabel("Branche");
		tradeBox.setDisplayField("trade");
		tradeBox.setTypeAhead(true);
		tradeBox.setStore(tradeStore);
		tradeBox.setTriggerAction(TriggerAction.ALL);
		companyForm.add(tradeBox);

		final SimpleComboBox<Importance> importanceBox = new SimpleComboBox<Importance>();
		importanceBox.setFieldLabel("Gruppe");
		importanceBox.add(Arrays.asList(Importance.values()));
		importanceBox.setTriggerAction(TriggerAction.ALL);
		companyForm.add(importanceBox);

		TextArea commentsFld = new TextArea();
		commentsFld.setFieldLabel("Kommentarer");
		commentsFld.setName("comments");
		commentsFld.setBorders(false);
		companyForm.add(commentsFld);

		final FormBinding binding = new FormBinding(companyForm, true);

		companyGrid.getSelectionModel().addListener(Events.SelectionChange,
				new Listener<SelectionChangedEvent<Company>>() {
					public void handleEvent(SelectionChangedEvent<Company> be) {
						if (be.getSelection().size() > 0) {
							binding.bind(be.getSelectedItem());
							importanceBox.setSimpleValue(be.getSelectedItem()
									.getImportance());

							City city = cityStore.findModel("postal", be
									.getSelectedItem().getPostal());
							ArrayList<City> citySelect = new ArrayList<City>();
							citySelect.add(city);
							postalBox.setSelection(citySelect);
						} else {
							binding.unbind();
						}
					}
				});

		return companyForm;
	}

	private FormPanel createEastMailForm() {
		FormPanel mailForm = new FormPanel();
		mailForm.setHeading("Mailindstillinger");
		
		return mailForm;
	}
	
	
	private void krHandleError(Throwable t) {
		t.printStackTrace();

		Dialog errorMessage = new Dialog();
		errorMessage.setTitle("Kunne ikke hente virksomhedsdata");
		errorMessage.setButtons(Dialog.OK);
		errorMessage.setBodyStyle("pad-text");
		errorMessage.setScrollMode(Scroll.AUTO);
		errorMessage.setHideOnButtonClick(true);
		errorMessage.addText("Beskeden fra systemet er:\n\"" + t.getMessage()
				+ "\"\nsystemet skulle gerne have lavet en log\n"
				+ "til ham den flinke på hjul.");

		errorMessage.show();

		// Log.debug(t);
	}
}
