package as.markon.client;

import java.util.ArrayList;
import java.util.Arrays;

import as.markon.viewmodel.City;
import as.markon.viewmodel.Company;
import as.markon.viewmodel.Importance;
import as.markon.viewmodel.Trade;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CreateCompany extends LayoutContainer {
	private DataServiceAsync dataService;
	private Company newCompany;

	public CreateCompany() {
		dataService = Global.getInstance().getDataService();
		
		newCompany = new Company();
		newCompany.setImportance(Importance.I);
		
		createNewCompanyPanel();
		createNewContactsPanel();
	}

	private void createNewCompanyPanel() {
		final FormPanel formPanel = new FormPanel();
		formPanel.setHeading("Indtast virksomhedsoplysninger");

		TextField<String> companynameFld = new TextField<String>();
		companynameFld.setFieldLabel("Firmanavn:");
		companynameFld.setName("companyname");
		companynameFld.setAllowBlank(false);
		formPanel.add(companynameFld);

		TextField<String> addressFld = new TextField<String>();
		addressFld.setFieldLabel("Adresse:");
		addressFld.setName("address");
		formPanel.add(addressFld);

		final ListStore<City> cityStore = new ListStore<City>();
		cityStore.setMonitorChanges(true);
		dataService.getCities(new AsyncCallback<ArrayList<City>>() {
			public void onSuccess(ArrayList<City> result) {
				cityStore.add(result);
				cityStore.sort("postal", SortDir.ASC);
			}

			public void onFailure(Throwable caught) {
				throw new RuntimeException(caught);
			}
		});
		
		final ComboBox<City> postalBox = new ComboBox<City>();
		postalBox.setFieldLabel("Postnummer");
		postalBox.setDisplayField("postal");
		postalBox.setTypeAhead(true);
		postalBox.setStore(cityStore);
		postalBox.setTriggerAction(TriggerAction.ALL);
		formPanel.add(postalBox);

		final ComboBox<City> cityBox = new ComboBox<City>();
		cityBox.setFieldLabel("By");
		cityBox.setDisplayField("cityname");
		cityBox.setTypeAhead(true);
		cityBox.setStore(cityStore);
		cityBox.setTriggerAction(TriggerAction.ALL);
		formPanel.add(cityBox);

		postalBox.addSelectionChangedListener(new SelectionChangedListener<City>() {
					@Override
					public void selectionChanged(SelectionChangedEvent<City> se) {
						newCompany.setPostal(se.getSelectedItem().getPostal());
						
						if (cityBox.getSelection().equals(se.getSelection()))
							return;
						cityBox.setSelection(se.getSelection());
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

		TextField<String> phoneFld = new TextField<String>();
		phoneFld.setFieldLabel("Telefon:");
		phoneFld.setName("phone");
		formPanel.add(phoneFld);

		TextField<String> mailFld = new TextField<String>();
		mailFld.setFieldLabel("Mail:");
		mailFld.setName("mail");
		formPanel.add(mailFld);

		final ListStore<Trade> tradeStore = new ListStore<Trade>();

		dataService.getTrades(new AsyncCallback<ArrayList<Trade>>() {
			public void onSuccess(ArrayList<Trade> result) {
				tradeStore.removeAll();
				tradeStore.add(result);
			}

			public void onFailure(Throwable caught) {
				throw new RuntimeException(caught);
			}
		});

		ComboBox<Trade> tradeBox = new ComboBox<Trade>();
		tradeBox.setFieldLabel("Branche:");
		tradeBox.setDisplayField("trade");
		tradeBox.setName("trade");
		tradeBox.setTypeAhead(true);
		tradeBox.setStore(tradeStore);
		tradeBox.setTriggerAction(TriggerAction.ALL);
		formPanel.add(tradeBox);

		final SimpleComboBox<Importance> importanceBox = new SimpleComboBox<Importance>();
		importanceBox.setFieldLabel("Gruppe:");
		importanceBox.add(Arrays.asList(Importance.values()));
		importanceBox.setTriggerAction(TriggerAction.ALL);
		importanceBox.setSimpleValue(newCompany.getImportance());
		
		importanceBox.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<Importance>>() {
					@Override
					public void selectionChanged(
							SelectionChangedEvent<SimpleComboValue<Importance>> se) {
						newCompany.setImportance(importanceBox.getSimpleValue());
					}
				});
		formPanel.add(importanceBox);

		TextArea commentsFld = new TextArea();
		commentsFld.setFieldLabel("Kommentarer:");
		commentsFld.setName("comments");
		formPanel.add(commentsFld);

		FormBinding binding = new FormBinding(formPanel);
		binding.autoBind();
		binding.bind(newCompany);

		formPanel.addButton(new Button("Opret firma",
				new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						dataService.createCompany(newCompany, new AsyncCallback<Integer>() {
							public void onSuccess(Integer result) {
								newCompany.set("companyid", result);
								
								fireEvent(Events.Add);
								fireEvent(Events.Close);
							}
							
							public void onFailure(Throwable caught) {
								throw new RuntimeException(caught);
							}
						});
					}
				}));
		
		formPanel.addButton(new Button("Anuller", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				fireEvent(Events.Close);
			}
		}));

		this.add(formPanel);
	}
	
	private void createNewContactsPanel() {
		final FormPanel contactsPanel = new FormPanel();
		
		this.add(contactsPanel);
	}
	
	public Company getNewCompany() {
		return newCompany;
	}
}
