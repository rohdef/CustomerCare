package as.markon.client;

import java.util.ArrayList;

import as.markon.viewmodel.Salesman;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SalesmanAdminWindow extends Window {

	private Grid<Salesman> salespeopleGrid;
	private DataServiceAsync dataService;
	private boolean loadingSalespeople = false;
	private LoadingDialog loader = new LoadingDialog();
	private ListStore<Salesman> salespeopleStore;
	private FormPanel editorArea;
	private FormPanel createArea;
	private FormBinding editBinding;

	public SalesmanAdminWindow() {
		this.setHeading("Administrer sælgere");
		this.setLayout(new RowLayout(Orientation.HORIZONTAL));
		this.setWidth(625);
		this.setHeight(270);
		
		salespeopleStore = new ListStore<Salesman>();
		
		ColumnConfig nameConfig = new ColumnConfig("salesman", "Navn", 150);
		ColumnConfig titleConfig = new ColumnConfig("title", "Titel", 150);
		
		ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		configs.add(nameConfig);
		configs.add(titleConfig);
		
		ColumnModel cm = new ColumnModel(configs);
		
		salespeopleGrid = new Grid<Salesman>(salespeopleStore, cm);
		salespeopleGrid.setAutoHeight(true);
		salespeopleGrid.setBorders(false);
		salespeopleGrid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		salespeopleGrid.setStripeRows(true);
		salespeopleGrid.getView().setEmptyText("Der er ingen salgsfolk i listen.");
		
		this.add(salespeopleGrid);
		
		ContentPanel editArea = new ContentPanel();
		editArea.setLayout(new AccordionLayout());
		editArea.setHeaderVisible(false);
		editArea.setWidth(310);
		editArea.setHeight(240);
		
		createArea = getCreateArea();
		editArea.add(createArea);
		editorArea = getEditArea();
		editArea.add(editorArea);
		
		this.add(editArea);
		
		dataService = Global.getInstance().getDataService();
	}
	
	@Override
	public void show() {
		super.show();
		
		loadingSalespeople = true;
		checkLoading();
		
		dataService.getSalesmen(new AsyncCallback<ArrayList<Salesman>>() {
			public void onSuccess(ArrayList<Salesman> result) {
				salespeopleStore.setMonitorChanges(true);
				salespeopleStore.add(result);
				loadingSalespeople = false;
				checkLoading();
			}
			
			public void onFailure(Throwable caught) {
			}
		});
		
		salespeopleGrid.getSelectionModel().addListener(Events.SelectionChange, 
				new Listener<SelectionChangedEvent<Salesman>>() {
					public void handleEvent(SelectionChangedEvent<Salesman> be) {
						editorArea.expand();
						createArea.collapse();
						editBinding.bind(be.getSelectedItem());
					}
		});
	}
	
	private FormPanel getCreateArea() {
		FormPanel editArea = new FormPanel();
		editArea.setHeading("Opret ny sælger");
		editArea.setFrame(false);
		editArea.setBorders(false);
		
		final TextField<String> nameFld = new TextField<String>();
		nameFld.setFieldLabel("Navn:");
		nameFld.setAllowBlank(false);
		nameFld.setValidator(new VTypeValidator(VType.NAME));
		editArea.add(nameFld);

		final TextField<String> titleFld = new TextField<String>();
		titleFld.setFieldLabel("Titel:");
		titleFld.setAllowBlank(false);
		titleFld.setValidator(new VTypeValidator(VType.ALPHABET));
		editArea.add(titleFld);

		final TextField<String> mailFld = new TextField<String>();
		mailFld.setFieldLabel("E-mail");
		mailFld.setAllowBlank(false);
		mailFld.setValidator(new VTypeValidator(VType.EMAIL));
		editArea.add(mailFld);

		final TextField<String> phoneFld = new TextField<String>();
		phoneFld.setFieldLabel("Mobil:");
		phoneFld.setValidator(new VTypeValidator(VType.PHONE));
		editArea.add(phoneFld);

		Button createBtn = new Button();
		createBtn.setText("Opret sælger");
		createBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				final Salesman newSalesman = new Salesman();
				newSalesman.setSalesman(nameFld.getValue());
				newSalesman.setTitle(titleFld.getValue());
				newSalesman.setMail(mailFld.getValue());
				newSalesman.setPhone(phoneFld.getValue());
				
				dataService.insertSalesman(newSalesman, new AsyncCallback<Integer>() {
					public void onSuccess(Integer result) {
						newSalesman.set("salesmanid", result);
						salespeopleStore.add(newSalesman);
					}
					
					public void onFailure(Throwable caught) {
					}
				});
			}
		});
		editArea.add(createBtn);
		
		return editArea;
	}
	
	private FormPanel getEditArea() {
		FormPanel editArea = new FormPanel();
		editArea.setHeading("Sælgerdetaljer");
		editArea.setFrame(false);
		editArea.setBorders(false);
		
		TextField<String> nameFld = new TextField<String>();
		nameFld.setFieldLabel("Navn:");
		nameFld.setName("salesman");
		nameFld.setAllowBlank(false);
		nameFld.setValidator(new VTypeValidator(VType.NAME));
		editArea.add(nameFld);

		TextField<String> titleFld = new TextField<String>();
		titleFld.setFieldLabel("Titel:");
		titleFld.setName("title");
		titleFld.setAllowBlank(false);
		titleFld.setValidator(new VTypeValidator(VType.ALPHABET));
		editArea.add(titleFld);

		TextField<String> mailFld = new TextField<String>();
		mailFld.setFieldLabel("E-mail");
		mailFld.setName("mail");
		mailFld.setAllowBlank(false);
		mailFld.setValidator(new VTypeValidator(VType.EMAIL));
		editArea.add(mailFld);

		TextField<String> phoneFld = new TextField<String>();
		phoneFld.setFieldLabel("Mobil:");
		phoneFld.setName("phone");
		phoneFld.setValidator(new VTypeValidator(VType.PHONE));
		editArea.add(phoneFld);
		
		editBinding = new FormBinding(editArea);
		editBinding.autoBind();
		
		editBinding.addListener(Events.UnBind, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				dataService.updateSalesman((Salesman) editBinding.getModel(),
						new AsyncCallback<Void>() {
							public void onSuccess(Void result) {
							}
							
							public void onFailure(Throwable caught) {
							}
						});
			}
		});
		
		Button deleteBtn = new Button();
		deleteBtn.setText("Slet sælger");
		deleteBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				Dialog dialog = new Dialog();
				dialog.setButtons(Dialog.YESNO);
				dialog.setHeading("Vil du slette "+editBinding.getModel().get("salesman"));
				dialog.addText("Er du sikker på, at du vil slette "
						+editBinding.getModel().get("salesman")+"? Denne handling"
						+"kan ikke fortrydes!");
				dialog.setHideOnButtonClick(true);
				
				dialog.getButtonById(Dialog.NO).setText("Fortryd");
				dialog.getButtonById(Dialog.YES).setText("Slet "+editBinding
						.getModel().get("salesman"));
				dialog.getButtonById(Dialog.YES).addSelectionListener(
						new SelectionListener<ButtonEvent>() {
							@Override
							public void componentSelected(ButtonEvent ce) {
								dataService.deleteSalesman((Salesman) editBinding.getModel(),
										new AsyncCallback<Void>() {
											public void onSuccess(Void result) {
												salespeopleStore.remove(
														(Salesman)editBinding.getModel());
											}
											
											public void onFailure(Throwable caught) {
											}
										});
							}
						});
				
				dialog.show();
			}
		});
		
		editArea.add(deleteBtn);

		return editArea;
	}
	
	private void checkLoading() {
		if (loadingSalespeople)
			loader.show();
		else
			loader.hide();
	}
}