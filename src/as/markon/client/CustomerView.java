package as.markon.client;

import java.util.ArrayList;
import java.util.List;

import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.binding.SimpleComboBoxFieldBinding;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;

public class CustomerView extends LayoutContainer {
	private ColumnModel cm;
	private final Grid<Company> companyGrid;
	
	public CustomerView() {
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
		
		
		// CENTER
		ContentPanel centerPanel = new ContentPanel();
		centerPanel.setHeading("Virksomheder");
		
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		
		ColumnConfig column = new ColumnConfig("companyname", "Et Firmanavn", 300);
		column.setRowHeader(true);
		configs.add(column);
		
		column = new ColumnConfig("city", "Postnr og by", 100);
		configs.add(column);
		
		cm = new ColumnModel(configs);
		
		
		ListStore<Company> companyStore = new ListStore<Company>();
		companyStore.add(DemoDataService.getCompanies());

		companyGrid = new Grid<Company>(companyStore, cm);
		companyGrid.setAutoExpandColumn("companyname");
		companyGrid.setAutoHeight(true);
		companyGrid.setBorders(false);
		companyGrid.setColumnLines(true);
		companyGrid.setColumnReordering(true);
		companyGrid.setStripeRows(true);
		
		GridSelectionModel<Company> sm = new GridSelectionModel<Company>();
		sm.setSelectionMode(SelectionMode.SINGLE);
		companyGrid.setSelectionModel(sm);
		
		centerPanel.add(companyGrid);
		
		
		// EAST
		ContentPanel eastPanel = getEastPnael();
		
		this.add(northPanel, new BorderLayoutData(LayoutRegion.NORTH, 100));
		this.add(centerPanel, new BorderLayoutData(LayoutRegion.CENTER, 0.7f));
		this.add(eastPanel, new BorderLayoutData(LayoutRegion.EAST, 0.3f));
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
		contactsBox.setStore(emptyStore);
		contactsForm.add(contactsBox);
		
		TextField<String> nameFld = new TextField<String>();
		nameFld.setBorders(false);
		nameFld.setFieldLabel("Navn");
		contactsForm.add(nameFld);
		
		TextField<String> titleFld = new TextField<String>();
		titleFld.setBorders(false);
		titleFld.setFieldLabel("Titel");
		contactsForm.add(titleFld);
		
		TextField<String> phoneFld = new TextField<String>();
		phoneFld.setBorders(false);
		phoneFld.setFieldLabel("Telefon");
		contactsForm.add(phoneFld);
		
		TextField<String> mailFld = new TextField<String>();
		mailFld.setBorders(false);
		mailFld.setFieldLabel("Mail");
		contactsForm.add(mailFld);
		
		// TODO kommentarer

		
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
		contactBinding.addFieldBinding(new FieldBinding(nameFld, "contactname"));
		contactBinding.addFieldBinding(new FieldBinding(titleFld, "title"));
		contactBinding.addFieldBinding(new FieldBinding(phoneFld, "phone"));
		contactBinding.addFieldBinding(new FieldBinding(mailFld, "mail"));
		
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
		companyForm.add(addressFld);
		
		TextField<String> cityFld = new TextField<String>();
		cityFld.setBorders(false);
		cityFld.setFieldLabel("Postnr./by");
		companyForm.add(cityFld);
		
		TextField<String> phoneFld = new TextField<String>();
		phoneFld.setBorders(false);
		phoneFld.setFieldLabel("Telefon");
		companyForm.add(phoneFld);
		
		TextField<String> mailFld = new TextField<String>();
		mailFld.setBorders(false);
		mailFld.setFieldLabel("Mail");
		companyForm.add(mailFld);

		// TODO importance (gruppe)
		// TODO comments
		
		final FormBinding binding = new FormBinding(companyForm, true);
		binding.addFieldBinding(new FieldBinding(addressFld, "address"));
		binding.addFieldBinding(new FieldBinding(cityFld, "city"));
		binding.addFieldBinding(new FieldBinding(phoneFld, "phone"));
		binding.addFieldBinding(new FieldBinding(mailFld, "mail"));
		
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
}
