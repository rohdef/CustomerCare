package dk.rohdef.client;

import java.util.ArrayList;


import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;

import dk.rohdef.client.services.DataServiceAsync;
import dk.rohdef.client.services.Global;
import dk.rohdef.client.specialtypes.VType;
import dk.rohdef.client.specialtypes.VTypeValidator;
import dk.rohdef.viewmodel.Salesman;

public class SalesmanAdminWindow extends Window {
	private Salesman originalSalesman;
	private Grid<Salesman> salespeopleGrid;
	private DataServiceAsync dataService;
	private boolean loadingSalespeople = false;
	private LoadingDialog loader = new LoadingDialog();
	private ListStore<Salesman> salespeopleStore;
	private FormPanel editorArea;
	private FormPanel createArea;
	private FormBinding editBinding;
	private FormButtonBinding editButtonBinding;

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
						if (be.getSelection().size() == 0)
							return;
						
						editorArea.expand();
						createArea.collapse();
						
						originalSalesman = be.getSelectedItem();
						Salesman editSalesman = new Salesman();
						editSalesman.setProperties(originalSalesman.getProperties());
						editBinding.bind(editSalesman);
					}
		});
	}
	
	private FormPanel getCreateArea() {
		final FormPanel editArea = new FormPanel();
		editArea.setHeading("Opret ny sælger");
		editArea.setFrame(false);
		editArea.setBorders(false);
		
		final TextField<String> nameFld = new TextField<String>();
		nameFld.setFieldLabel("Navn:");
		nameFld.setAllowBlank(false);
		nameFld.setValidator(new VTypeValidator(VType.NAME));
		nameFld.setAutoValidate(true);
		editArea.add(nameFld);

		final TextField<String> titleFld = new TextField<String>();
		titleFld.setFieldLabel("Titel:");
		titleFld.setAllowBlank(false);
		titleFld.setValidator(new VTypeValidator(VType.ALPHABET));
		titleFld.setAutoValidate(true);
		editArea.add(titleFld);

		final TextField<String> mailFld = new TextField<String>();
		mailFld.setFieldLabel("E-mail");
		mailFld.setAllowBlank(false);
		mailFld.setValidator(new VTypeValidator(VType.EMAIL));
		mailFld.setAutoValidate(true);
		editArea.add(mailFld);

		final TextField<String> phoneFld = new TextField<String>();
		phoneFld.setFieldLabel("Mobil:");
		phoneFld.setValidator(new VTypeValidator(VType.PHONE));
		phoneFld.setAutoValidate(true);
		editArea.add(phoneFld);

		Button createBtn = new Button();
		createBtn.setText("Opret sælger");
		createBtn.setText("Submit");
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
						editArea.clear();
					}
					
					public void onFailure(Throwable caught) {
					}
				});
			}
		});
		editArea.add(createBtn);
		
		FormButtonBinding buttonBinding = new FormButtonBinding(editArea);
		buttonBinding.addButton(createBtn);
		
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
		nameFld.setAutoValidate(true);
		nameFld.setValidator(new VTypeValidator(VType.NAME));
		editArea.add(nameFld);

		TextField<String> titleFld = new TextField<String>();
		titleFld.setFieldLabel("Titel:");
		titleFld.setName("title");
		titleFld.setAllowBlank(false);
		titleFld.setAutoValidate(true);
		titleFld.setValidator(new VTypeValidator(VType.ALPHABET));
		editArea.add(titleFld);

		TextField<String> mailFld = new TextField<String>();
		mailFld.setFieldLabel("E-mail");
		mailFld.setName("mail");
		mailFld.setAllowBlank(false);
		mailFld.setAutoValidate(true);
		mailFld.setValidator(new VTypeValidator(VType.EMAIL));
		editArea.add(mailFld);

		TextField<String> phoneFld = new TextField<String>();
		phoneFld.setFieldLabel("Mobil:");
		phoneFld.setName("phone");
		phoneFld.setAutoValidate(true);
		phoneFld.setValidator(new VTypeValidator(VType.PHONE));
		editArea.add(phoneFld);
		
		editBinding = new FormBinding(editArea, true);
		
		editButtonBinding = new FormButtonBinding(editArea);
		
		editArea.setTopComponent(getEditToolBar());

		return editArea;
	}
	
	private ToolBar getEditToolBar() {
		ToolBar toolBar = new ToolBar();
		
		Button saveBtn = new Button();
		saveBtn.setText("Gem ændringer");
		saveBtn.setIcon(IconHelper.createPath("images/accept.gif"));
		saveBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				save();
			}
		});
		toolBar.add(saveBtn);
		editButtonBinding.addButton(saveBtn);
		
		final Button deleteBtn = new Button();
		deleteBtn.setText("Slet sælger");
		deleteBtn.disable();
		deleteBtn.setIcon(IconHelper.createPath("images/user_delete.gif"));
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
								delete();
							}
						});
				
				dialog.show();
			}
		});
		
		editBinding.addListener(Events.Bind, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				if (Global.getInstance().getCurrentSalesman().equals(editBinding.getModel()))
					deleteBtn.disable();
				else
					deleteBtn.enable();
			}
		});
		editBinding.addListener(Events.UnBind, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				deleteBtn.disable();
			}
		});
		
		toolBar.add(deleteBtn);
		
		return toolBar;
	}
	
	private void save() {
		dataService.updateSalesman((Salesman) editBinding.getModel(),
				new AsyncCallback<Void>() {
					public void onSuccess(Void result) {
						originalSalesman.setProperties(editBinding.getModel()
								.getProperties());
					}
					
					public void onFailure(Throwable caught) {
					}
				});
	}
	
	private void delete() {
		dataService.deleteSalesman(originalSalesman,
				new AsyncCallback<Void>() {
					public void onSuccess(Void result) {
						salespeopleStore.remove(originalSalesman);
						editBinding.unbind();
						originalSalesman = null;
					}
					
					public void onFailure(Throwable caught) {
						caught.printStackTrace();
					}
				});
	}
	
	private void checkLoading() {
		if (loadingSalespeople)
			loader.show();
		else
			loader.hide();
	}
}