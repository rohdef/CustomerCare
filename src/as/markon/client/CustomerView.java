package as.markon.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;
import as.markon.viewmodel.Importance;
import as.markon.viewmodel.Salesman;
import as.markon.viewmodel.Trade;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
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
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CustomerView extends LayoutContainer {
	private ListStore<Company> companyStore = new ListStore<Company>();
	private DataServiceAsync dataService = GWT.create(DataService.class);
	private Salesman salesman;
	
	public Salesman getSalesman() {
		return salesman;
	}

	public void setSalesman(Salesman salesman) {
		this.salesman = salesman;
		
		dataService.getCompanies(salesman.getSalesmanid(), new AsyncCallback<ArrayList<Company>>() {
			public void onSuccess(ArrayList<Company> result) {
				companyStore.removeAll();
				companyStore.add(result);
			}

			public void onFailure(Throwable caught) {
				krHandleError(caught);
			}
		});
	}

	private ColumnModel cm;
	private Grid<Company> companyGrid;

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

		// REST
		ContentPanel centerPanel = createCenterPanel();
		ContentPanel eastPanel = getEastPnael();

		this.add(northPanel, new BorderLayoutData(LayoutRegion.NORTH, 100));
		this.add(centerPanel, new BorderLayoutData(LayoutRegion.CENTER, 0.7f));
		this.add(eastPanel, new BorderLayoutData(LayoutRegion.EAST, 0.3f));
	}

	private ContentPanel createCenterPanel() {
		ContentPanel centerPanel = new ContentPanel();
		FitLayout centerLayout = new FitLayout();
		centerPanel.setLayout(centerLayout);
		
		centerPanel.setHeading("Virksomheder");

		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		ColumnConfig column = new ColumnConfig("companyname", "Et Firmanavn",
				300);
		column.setRowHeader(true);
		configs.add(column);

		column = new ColumnConfig("postal", "Postnr", 50);
		configs.add(column);
		
		column = new ColumnConfig("city", "By", 150);
		configs.add(column);

		cm = new ColumnModel(configs);
		
		companyStore.setDefaultSort("companyname", SortDir.ASC);

		companyGrid = new Grid<Company>(companyStore, cm);
		companyGrid.setAutoExpandColumn("companyname");
		companyGrid.setBorders(false);
		companyGrid.setColumnLines(true);
		companyGrid.setColumnReordering(true);
		companyGrid.setStripeRows(true);

		GridSelectionModel<Company> sm = new GridSelectionModel<Company>();
		sm.setSelectionMode(SelectionMode.SINGLE);
		companyGrid.setSelectionModel(sm);

		centerPanel.setHeight(550);
		centerPanel.add(companyGrid);
		return centerPanel;
	}

	private ContentPanel getEastPnael() {
		ContentPanel eastPanel = new ContentPanel();
		eastPanel.setHeading("Firmadata");

		eastPanel.add(getEastCompanyForm());
		eastPanel.add(getEastContactsForm());

		return eastPanel;
	}

	private FormPanel getEastContactsForm() {
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
							contactStore.add(be.getSelectedItem().getContacts());

							contactsBox.setStore(contactStore);
						} else
							contactsBox.setStore(emptyStore);
					}
				});

		final FormBinding contactBinding = new FormBinding(contactsForm, true);
		
		contactsBox.addSelectionChangedListener(new SelectionChangedListener<Contact>() {
					@Override
					public void selectionChanged(SelectionChangedEvent<Contact> se) {
						contactBinding.bind(se.getSelectedItem());
					}
				});

		return contactsForm;
	}

	private FormPanel getEastCompanyForm() {
		FormPanel companyForm = new FormPanel();
		companyForm.setHeaderVisible(false);

		TextField<String> addressFld = new TextField<String>();
		addressFld.setBorders(false);
		addressFld.setFieldLabel("Adresse");
		addressFld.setName("address");
		companyForm.add(addressFld);

		TextField<String> postalFld = new TextField<String>();
		postalFld.setBorders(false);
		postalFld.setFieldLabel("Postnr.");
		postalFld.setName("postal");
		companyForm.add(postalFld);

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
		
		SimpleComboBox<Importance> importanceBox = new SimpleComboBox<Importance>();
		importanceBox.setFieldLabel("Gruppe");
		importanceBox.add(Arrays.asList(Importance.values()));
//		importanceBox.setSimpleValue(...) // Set the value to whatever the database says
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
						if (be.getSelection().size() > 0)
							binding.bind(be.getSelectedItem());
						else
							binding.unbind();
					}
				});

		return companyForm;
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
