package dk.rohdef.client.panels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

import dk.rohdef.client.CreateCompany;
import dk.rohdef.client.i18n.CustomerCareI18n;
import dk.rohdef.client.services.DataServiceAsync;
import dk.rohdef.client.services.Global;
import dk.rohdef.viewmodel.City;
import dk.rohdef.viewmodel.Company;
import dk.rohdef.viewmodel.Importance;
import dk.rohdef.viewmodel.Trade;

/**
 * Panel to edit the informations about a company. 
 * @author Rohde Fischer <rohdef@rohdef.dk>
 */
public class CompanyEditPanel extends FormPanel {
	private DataServiceAsync dataService = Global.getInstance().getDataService();
	private CustomerCareI18n i18n = Global.getInstance().getI18n();
	
	private ListStore<City> cityStore;
	private Company original;
	private FormBinding binding;
	private SimpleComboBox<Importance> importanceBox;
	private ComboBox<Trade> tradeBox;
	private ListStore<Trade> tradeStore;
//	private TextField<String> phoneFld;
	private ComboBox<City> cityBox;
	private ComboBox<City> postalBox;
	private TextField<String> addressFld;
	private TextField<String> companynameFld;
	private PhoneNumberPanel phonePanel;

	/**
	 * 
	 */
	public CompanyEditPanel() {
		this.setHeading(i18n.companyData());
		binding = new FormBinding(this);
		
		companynameFld = new TextField<String>();
		companynameFld.setFieldLabel(i18n.companyName());
		companynameFld.setName("companyname");
		companynameFld.setAllowBlank(false);
		companynameFld.setAutoValidate(true);
		this.add(companynameFld);
		
		addressFld = new TextField<String>();
		addressFld.setFieldLabel(i18n.address());
		addressFld.setName("address");
		this.add(addressFld);

		cityStore = new ListStore<City>();
		dataService.getCities(new AsyncCallback<ArrayList<City>>() {
			public void onSuccess(ArrayList<City> result) {
				cityStore.add(result);
				cityStore.sort("postal", SortDir.ASC);
			}

			public void onFailure(Throwable caught) {
				throw new RuntimeException(caught);
			}
		});
		
		postalBox = new ComboBox<City>();
		postalBox.setFieldLabel(i18n.postal());
		postalBox.setDisplayField("postal");
		postalBox.setTypeAhead(true);
		postalBox.setStore(cityStore);
		postalBox.setTriggerAction(TriggerAction.ALL);
		this.add(postalBox);

		cityBox = new ComboBox<City>();
		cityBox.setFieldLabel(i18n.city());
		cityBox.setDisplayField("cityname");
		cityBox.setTypeAhead(true);
		cityBox.setStore(cityStore);
		cityBox.setTriggerAction(TriggerAction.ALL);
		this.add(cityBox);

		postalBox.addSelectionChangedListener(new SelectionChangedListener<City>() {
					@Override
					public void selectionChanged(SelectionChangedEvent<City> se) {
						if (cityBox.getSelection().equals(se.getSelection()))
							return;
						cityBox.setSelection(se.getSelection());
					}
				});

		postalBox.addSelectionChangedListener(new SelectionChangedListener<City>() {
					@Override
					public void selectionChanged(SelectionChangedEvent<City> se) {
						if (binding.getModel() != null
								&& se.getSelectedItem() != null) {
							binding.getModel().set("postal",
									se.getSelectedItem().getPostal());
							binding.getModel().set("city",
									se.getSelectedItem().getCity());
						}
					}
				});

		cityBox.addSelectionChangedListener(new SelectionChangedListener<City>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<City> se) {
				if (postalBox.getSelection().equals(se.getSelection()))
					return;
				postalBox.setSelection(se.getSelection());
			}
		});

		phonePanel = new PhoneNumberPanel();
		this.add(phonePanel);		
		
		tradeStore = new ListStore<Trade>();
		tradeBox = new ComboBox<Trade>();
		dataService.getTrades(new AsyncCallback<ArrayList<Trade>>() {
			public void onSuccess(ArrayList<Trade> result) {
				tradeStore.add(result);
				Trade t = new Trade();
				t.setTrade(i18n.noTradeSelected());
				tradeStore.add(t);
			}

			public void onFailure(Throwable caught) {
				throw new RuntimeException(caught);
			}
		});

		tradeBox.setFieldLabel(i18n.trade());
		tradeBox.setDisplayField("trade");
		tradeBox.setName("trade");
		tradeBox.setTypeAhead(true);
		tradeBox.setStore(tradeStore);
		tradeBox.setTriggerAction(TriggerAction.ALL);
		this.add(tradeBox);
		
		importanceBox = new SimpleComboBox<Importance>();
		importanceBox.setFieldLabel(i18n.group());
		importanceBox.add(Arrays.asList(Importance.values()));
		importanceBox.setTriggerAction(TriggerAction.ALL);

		importanceBox.addSelectionChangedListener(
				new SelectionChangedListener<SimpleComboValue<Importance>>() {
					@Override
					public void selectionChanged(
							SelectionChangedEvent<SimpleComboValue<Importance>> se) {
						if (binding.getModel() != null)
							((Company) binding.getModel())
								.setImportance(importanceBox.getSimpleValue());
					}
				});

		this.add(importanceBox);

		TextArea commentsFld = new TextArea();
		commentsFld.setFieldLabel(i18n.comments());
		commentsFld.setName("comments");
		this.add(commentsFld);
		
		this.add(phonePanel.getValidatorField());
		
		this.setReadOnly(true);
		phonePanel.setReadOnly(true);
		binding.autoBind();
	}
	
	/**
	 * Attach a company to the panel to show the data and enable it for editing.
	 * @param company the company to edit.
	 */
	public void bindCompany(Company company) {
		this.original = company;
		Company dataClone = new Company();
		dataClone.setProperties(company.getProperties());
		
		binding.bind(dataClone);
		importanceBox.setSimpleValue(dataClone.getImportance());
		
		City city = cityStore.findModel("postal", dataClone.getPostal());
		ArrayList<City> citySelect = new ArrayList<City>();
		citySelect.add(city);
		postalBox.setSelection(citySelect);
		phonePanel.setPhoneNumbers(company.getPhones());
		this.setReadOnly(false);
		phonePanel.setReadOnly(false);
	}

	/**
	 * Remove the company details from the panel without saving it.
	 */
	public void unbindCompany() {
		binding.unbind();
		importanceBox.clear();
		cityBox.clear();
		postalBox.clear();
		phonePanel.reset();
		original = null;
		this.setReadOnly(true);
		phonePanel.setReadOnly(true);
	}
	
	/**
	 * Get the original model (this is the one that components might listen to).
	 * @see {@link #getShownModel()}
	 * @return
	 */
	public Company getOriginalModel() {
		return original;
	}
	
	/**
	 * Get the model as it is show in the form.
	 * @see {@link #getOriginalModel()}
	 * @return
	 */
	public Company getShownModel() {
		((Company) binding.getModel()).setPhones(phonePanel.getPhoneNumbers());
		return (Company) binding.getModel();
	}

	/**
	 * Save the data from the form into the model. This will cause any element obersving 
	 * the model to be updated.
	 */
	public void saveToModel() {
		Company c = ((Company) binding.getModel());
		List<String> phones = phonePanel.getPhoneNumbers();
		c.setPhones(phones);
		original.setProperties(c.getProperties());
	}
	
	/**
	 * Allows reacting to changes in the company name field. This is used for the automatic 
	 * searching in create company.
	 * @see {@link CreateCompany}
	 * @param listener
	 */
	public void addCompanyNameFieldKeyListener(Listener<FieldEvent> listener) {
		companynameFld.addListener(Events.KeyPress, listener);
	}
	
	/**
	 * Removes a listener for company names.
	 * @see {@link #addCompanyNameFieldKeyListener(Listener)}
	 * @param listener
	 */
	public void removeCompanyNameFieldKeyListener(Listener<FieldEvent> listener) {
		companynameFld.removeListener(Events.KeyPress, listener);
	}
	
	/**
	 * Get the value from the company name field (this is due to delays in updating the 
	 * model.
	 * @return
	 */
	public String getCompanyName() {
		return companynameFld.getValue();
	}
}
