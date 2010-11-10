package as.markon.client;

import java.util.ArrayList;
import java.util.List;

import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
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
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;

public class CustomerView extends LayoutContainer {
	private ColumnModel cm;
	
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
//		centerPanel.setLayout(new FitLayout());
		centerPanel.setHeading("Virksomheder");
		
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		
		ColumnConfig column = new ColumnConfig("companyname", "Et Firmanavn", 300);
		column.setRowHeader(true);
		configs.add(column);
		
		column = new ColumnConfig("city", "Postnr og by", 100);
		configs.add(column);
		
		cm = new ColumnModel(configs);
		
		
		ListStore<Company> companyStore = new ListStore<Company>();
		Company cmp;
		
		cmp = new Company();
		cmp.setName("Company 1");
		cmp.setCity("8000");
		companyStore.add(cmp);
		
		cmp = new Company();
		cmp.setName("Company 2");
		cmp.setCity("8200");
		companyStore.add(cmp);
		
		cmp = new Company();
		cmp.setName("Company 3");
		cmp.setCity("8200");
		companyStore.add(cmp);
		
		cmp = new Company();
		cmp.setName("Company 4");
		cmp.setCity("8000");
		companyStore.add(cmp);
		

		Grid<Company> companyGrid = new Grid<Company>(companyStore, cm);
		companyGrid.setAutoExpandColumn("companyname");
		companyGrid.setBorders(false);
		companyGrid.setStripeRows(true);
		companyGrid.setColumnLines(true);
		companyGrid.setColumnReordering(true);
		
		centerPanel.add(companyGrid);
		
		
		// EAST
		ContentPanel eastPanel = new ContentPanel();
//		VBoxLayout eastLayout = new VBoxLayout();
//		eastPanel.setLayout(eastLayout);
		eastPanel.setHeading("Firmadata");
		
		FormPanel companyForm = new FormPanel();
		companyForm.setHeaderVisible(false);
		
		TextField<String> tf = new TextField<String>();
		tf.setBorders(false);
		tf.setFieldLabel("Adresse");
		companyForm.add(tf);
		
		tf = new TextField<String>();
		tf.setBorders(false);
		tf.setFieldLabel("Postnr./by");
		companyForm.add(tf);
		
		tf = new TextField<String>();
		tf.setBorders(false);
		tf.setFieldLabel("Telefon");
		companyForm.add(tf);
		
		tf = new TextField<String>();
		tf.setBorders(false);
		tf.setFieldLabel("Mail");
		companyForm.add(tf);

		// TODO importance (gruppe)
		// TODO comments
		eastPanel.add(companyForm);
		
		FormPanel contactsForm = new FormPanel();
		contactsForm.setHeading("Kontakter");
	
		ListStore<Contact> contactStore = new ListStore<Contact>();
		Contact c = new Contact();
		c.setName("Biger");
		contactStore.add(c);
		
		c = new Contact();
		c.setName("Bent");
		contactStore.add(c);
		
		c = new Contact();
		c.setName("Hansi");
		contactStore.add(c);
		
		ComboBox<Contact> contactsBox = new ComboBox<Contact>();
		contactsBox.setFieldLabel("Kontaktliste");
		// setDisplayField
		// setStore
		contactsBox.setDisplayField("contactname");
		contactsBox.setStore(contactStore);
		contactsForm.add(contactsBox);
		
		tf = new TextField<String>();
		tf.setBorders(false);
		tf.setFieldLabel("Navn");
		contactsForm.add(tf);
		
		tf = new TextField<String>();
		tf.setBorders(false);
		tf.setFieldLabel("Titel");
		contactsForm.add(tf);
		
		tf = new TextField<String>();
		tf.setBorders(false);
		tf.setFieldLabel("Telefon");
		contactsForm.add(tf);
		
		tf = new TextField<String>();
		tf.setBorders(false);
		tf.setFieldLabel("Mail");
		contactsForm.add(tf);
		
		// TODO kommentarer
		
		eastPanel.add(contactsForm);
		
		this.add(northPanel, new BorderLayoutData(LayoutRegion.NORTH, 100));
		this.add(centerPanel, new BorderLayoutData(LayoutRegion.CENTER, 0.7f));
		this.add(eastPanel, new BorderLayoutData(LayoutRegion.EAST, 0.3f));
	}
}
