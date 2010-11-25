package as.markon.client;

import java.util.ArrayList;
import java.util.Arrays;

import javax.management.RuntimeErrorException;

import as.markon.viewmodel.City;
import as.markon.viewmodel.Company;
import as.markon.viewmodel.Importance;
import as.markon.viewmodel.Trade;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CompanyEditPanel extends FormPanel {
	private DataServiceAsync dataService = Global.getInstance()
			.getDataService();
	private ListStore<City> cityStore;
	private FormBinding binding;
	private SimpleComboBox<Importance> importanceBox;
	private ComboBox<Trade> tradeBox;
	private ListStore<Trade> tradeStore;
	private CheckBox acceptsMailsBox;
	private TextField<String> mailFld;
	private TextField<String> phoneFld;
	private ComboBox<City> cityBox;
	private ComboBox<City> postalBox;
	private TextField<String> addressFld;

	public CompanyEditPanel() {
		this.setHeading("Firmadata");
		binding = new FormBinding(this, true);

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

		addressFld = new TextField<String>();
		addressFld.setBorders(false);
		addressFld.setFieldLabel("Adresse");
		addressFld.setName("address");
		this.add(addressFld);

		postalBox = new ComboBox<City>();
		postalBox.setFieldLabel("Postnummer");
		postalBox.setDisplayField("postal");
		postalBox.setTypeAhead(true);
		postalBox.setStore(cityStore);
		postalBox.setTriggerAction(TriggerAction.ALL);
		this.add(postalBox);

		cityBox = new ComboBox<City>();
		cityBox.setFieldLabel("By");
		cityBox.setDisplayField("cityname");
		cityBox.setTypeAhead(true);
		cityBox.setStore(cityStore);
		cityBox.setTriggerAction(TriggerAction.ALL);
		this.add(cityBox);

		postalBox
				.addSelectionChangedListener(new SelectionChangedListener<City>() {
					@Override
					public void selectionChanged(SelectionChangedEvent<City> se) {
						if (cityBox.getSelection().equals(se.getSelection()))
							return;
						cityBox.setSelection(se.getSelection());
					}
				});

		postalBox
				.addSelectionChangedListener(new SelectionChangedListener<City>() {
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

		phoneFld = new TextField<String>();
		phoneFld.setBorders(false);
		phoneFld.setFieldLabel("Telefon");
		phoneFld.setName("phone");
		this.add(phoneFld);

		mailFld = new TextField<String>();
		mailFld.setBorders(false);
		mailFld.setFieldLabel("Mail");
		mailFld.setName("mail");
		this.add(mailFld);

		acceptsMailsBox = new CheckBox();
		acceptsMailsBox.setFieldLabel("Ønsker nyhedsmails:");
		acceptsMailsBox.setName("acceptsmails");
		this.add(acceptsMailsBox);

		tradeStore = new ListStore<Trade>();

		dataService.getTrades(new AsyncCallback<ArrayList<Trade>>() {
			public void onSuccess(ArrayList<Trade> result) {
				tradeStore.removeAll();
				tradeStore.add(result);
			}

			public void onFailure(Throwable caught) {
				throw new RuntimeException(caught);
			}
		});

		tradeBox = new ComboBox<Trade>();
		tradeBox.setFieldLabel("Branche:");
		tradeBox.setDisplayField("trade");
		tradeBox.setName("trade");
		tradeBox.setTypeAhead(true);
		tradeBox.setStore(tradeStore);
		tradeBox.setTriggerAction(TriggerAction.ALL);
		this.add(tradeBox);

		importanceBox = new SimpleComboBox<Importance>();
		importanceBox.setFieldLabel("Gruppe:");
		importanceBox.add(Arrays.asList(Importance.values()));
		importanceBox.setTriggerAction(TriggerAction.ALL);

		importanceBox
				.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<Importance>>() {
					@Override
					public void selectionChanged(
							SelectionChangedEvent<SimpleComboValue<Importance>> se) {
						if (binding.getModel() != null)
							((Company) binding.getModel())
									.setImportance(importanceBox
											.getSimpleValue());
					}
				});

		this.add(importanceBox);

		TextArea commentsFld = new TextArea();
		commentsFld.setFieldLabel("Kommentarer");
		commentsFld.setName("comments");
		commentsFld.setBorders(false);
		this.add(commentsFld);

		binding.addListener(Events.UnBind, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				Company company = (Company) binding.getModel();

				dataService.updateCompany(company, new AsyncCallback<Void>() {
					public void onSuccess(Void result) {
					}

					public void onFailure(Throwable caught) {
						throw new RuntimeException(caught);
					}
				});
			}
		});
	}

	public void bindCompany(Company company) {
		binding.autoBind();
		binding.bind(company);
		importanceBox.setSimpleValue(company.getImportance());

		City city = cityStore.findModel("postal", company.getPostal());
		ArrayList<City> citySelect = new ArrayList<City>();
		citySelect.add(city);
		postalBox.setSelection(citySelect);
	}

	public void unbindCompany() {
		binding.unbind();
		importanceBox.clear();
		cityBox.clear();
		postalBox.clear();
	}
}
