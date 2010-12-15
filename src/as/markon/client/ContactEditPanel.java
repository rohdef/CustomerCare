package as.markon.client;

import java.util.ArrayList;

import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;
import as.markon.viewmodel.Salesman;

import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ContactEditPanel extends FormPanel {
	private ListStore<Contact> emptyStore;
	private ListStore<Contact> contactStore;
	private ComboBox<Contact> contactsBox;
	private TextField<String> nameFld;
	private TextField<String> titleFld;
	private TextField<String> phoneFld;
	private TextField<String> mailFld;
	private CheckBox acceptsMailsBox;
	private TextArea commentFld;
	private FormBinding contactBinding;
	
	private DataServiceAsync dataService = Global.getInstance().getDataService();
	private LoadingDialog loader = new LoadingDialog();
	private Button changeSalesman;
	private Company company;
	private Button addContactBtn;
	private Button saveBtn;

	public ContactEditPanel() {
		this.setHeading("Kontakter");

		emptyStore = new ListStore<Contact>();

		contactsBox = new ComboBox<Contact>();
		contactsBox.setFieldLabel("Kontaktliste");
		contactsBox.setDisplayField("contactname");
		contactsBox.setTypeAhead(true);
		contactsBox.setStore(emptyStore);
		contactsBox.setTriggerAction(TriggerAction.ALL);
		this.add(contactsBox);
		
		nameFld = new TextField<String>();
		nameFld.setBorders(false);
		nameFld.setFieldLabel("Navn");
		nameFld.setName("contactname");
		nameFld.setAllowBlank(false);
		nameFld.setAutoValidate(true);
		nameFld.setValidationDelay(0);
		nameFld.setValidator(new VTypeValidator(VType.NAME));
		this.add(nameFld);

		titleFld = new TextField<String>();
		titleFld.setBorders(false);
		titleFld.setFieldLabel("Titel");
		titleFld.setName("title");
		titleFld.setAutoValidate(true);
		titleFld.setValidator(new VTypeValidator(VType.ALPHABET));
		this.add(titleFld);

		phoneFld = new TextField<String>();
		phoneFld.setBorders(false);
		phoneFld.setFieldLabel("Telefon");
		phoneFld.setName("phone");
		phoneFld.setAutoValidate(true);
		phoneFld.setValidator(new VTypeValidator(VType.PHONE));
		this.add(phoneFld);

		mailFld = new TextField<String>();
		mailFld.setBorders(false);
		mailFld.setFieldLabel("Mail");
		mailFld.setName("mail");
		mailFld.setAutoValidate(true);
		mailFld.setValidator(new VTypeValidator(VType.EMAIL));
		this.add(mailFld);

		acceptsMailsBox = new CheckBox();
		acceptsMailsBox.setFieldLabel("Ønsker mails");
		acceptsMailsBox.setName("acceptsmails");
		this.add(acceptsMailsBox);

		commentFld = new TextArea();
		commentFld.setBorders(false);
		commentFld.setFieldLabel("Kommentarer");
		commentFld.setName("comments");
		this.add(commentFld);

		contactBinding = new FormBinding(this, true);

		contactsBox.addSelectionChangedListener(
						new SelectionChangedListener<Contact>() {
					@Override
					public void selectionChanged(SelectionChangedEvent<Contact> se) {
						contactBinding.bind(se.getSelectedItem());
						setReadOnly(false);
						changeSalesman.enable();
					}
				});
		
		this.setReadOnly(true);	
		this.setTopComponent(getToolbar());
	}
	
	private void save() {
		Contact contact = (Contact) contactBinding.getModel();
		
		dataService.updateContact(contact, new AsyncCallback<Void>() {
			public void onSuccess(Void result) {
			}
			
			public void onFailure(Throwable caught) {
				throw new RuntimeException(caught);
			}
		});
	}
	
	private ToolBar getToolbar() {
		ToolBar toolbar = new ToolBar();
		FormButtonBinding buttonBinding = new FormButtonBinding(this);
		
		saveBtn = new Button("Gem");
		saveBtn.disable();
		saveBtn.setIcon(IconHelper.createPath("images/accept.gif"));
		saveBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				save();
			}
		});
		toolbar.add(saveBtn);
		buttonBinding.addButton(saveBtn);
		
		changeSalesman = new Button();
		changeSalesman.setText("Flyt til sælger");
		changeSalesman.setIcon(IconHelper.createPath("images/user_go.gif"));
		changeSalesman.disable();
		changeSalesman.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				final Dialog setSalesmanDialog = new Dialog();
				setSalesmanDialog.setTitle("Hvem ønsker du skal overtage kunden?");
				setSalesmanDialog.setButtons(Dialog.OKCANCEL);
				setSalesmanDialog.setHideOnButtonClick(true);
				
				FormPanel selectSalesmanPanel = new FormPanel();
				selectSalesmanPanel.setAutoWidth(true);
				selectSalesmanPanel.setHeaderVisible(false);
				
				final ListStore<Salesman> salesmanStore = new ListStore<Salesman>();
				final ComboBox<Salesman> salesmanBox = new ComboBox<Salesman>();
				salesmanBox.setDisplayField("salesman");
				salesmanBox.setFieldLabel("Vælg sælger");
				salesmanBox.setStore(salesmanStore);
				salesmanBox.setAutoWidth(true);
				dataService.getSalesmen(new AsyncCallback<ArrayList<Salesman>>() {
					public void onSuccess(ArrayList<Salesman> result) {
						salesmanStore.add(result);
						salesmanBox.setForceSelection(true);
						salesmanBox.select(Global.getInstance().getCurrentSalesman());
					}
					
					public void onFailure(Throwable caught) {
						throw new RuntimeException(caught);
					}
				});
				selectSalesmanPanel.add(salesmanBox);
				setSalesmanDialog.add(selectSalesmanPanel);
				
				setSalesmanDialog.getButtonById(Dialog.OK).setText("Skift saelger");
				setSalesmanDialog.getButtonById(Dialog.CANCEL).setText("Anuller");
				
				setSalesmanDialog.getButtonById(Dialog.OK).addSelectionListener(
						new SelectionListener<ButtonEvent>() {
							@Override
							public void componentSelected(ButtonEvent ce) {
								Salesman salesman = salesmanBox.getValue();
								((Contact)contactBinding.getModel()).setSalesman(salesman);
							}
						});
				
				setSalesmanDialog.show();
			}
		});
		toolbar.add(changeSalesman);
		buttonBinding.addButton(changeSalesman);
		
		addContactBtn = new Button();
		addContactBtn.setText("Tilføj kontakt");
		addContactBtn.setIcon(IconHelper.createPath("images/user_add.gif"));
		addContactBtn.disable();
		addContactBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				CreateContactWindow createContact =
					new CreateContactWindow((Integer) Global.getInstance()
							.getCurrentSalesman().get("salesmanid"),
							(Integer) company.get("companyid"));
				
				createContact.addListener(Events.Hide, new Listener<BaseEvent>() {
					public void handleEvent(BaseEvent be) {
						bindCompany(company, Global.getInstance().getCurrentSalesman());
					}
				});
				
				createContact.show();
			}
		});
		toolbar.add(addContactBtn);
		
		final Button deleteContactBtn = new Button();
		deleteContactBtn.setText("Slet kontakt");
		deleteContactBtn.setIcon(IconHelper.createPath("images/user_delete.gif"));
		deleteContactBtn.disable();
		deleteContactBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				Dialog deleteDialog = new Dialog();
				deleteDialog.setHideOnButtonClick(true);
				deleteDialog.setButtons(Dialog.YESNO);
				deleteDialog.setHeading("Er du sikker på, at du vil slette "+
						contactBinding.getModel().get("contactname"));
				deleteDialog.addText("Vil du slette "+
						contactBinding.getModel().get("contactname")+
						"? Denne handling kan ikke fortrydes!");
				
				deleteDialog.getButtonById(Dialog.NO).setText("Fortyd");
				deleteDialog.getButtonById(Dialog.YES).setText("Slet kontakten");
				deleteDialog.getButtonById(Dialog.YES).addSelectionListener(
						new SelectionListener<ButtonEvent>() {
							@Override
							public void componentSelected(ButtonEvent ce) {
								dataService.deleteContact((Contact)contactBinding.getModel(),
										new AsyncCallback<Void>() {
											public void onSuccess(Void result) {
												contactStore.remove(
														(Contact)contactBinding.getModel());
												contactBinding.unbind();
											}
											
											public void onFailure(Throwable caught) {
											}
										});
							}
						});
				
				deleteDialog.show();
			}
		});
		
		toolbar.add(deleteContactBtn);
		
		final Button sendMailBtn = new Button("Send mail");
		sendMailBtn.setIcon(IconHelper.createPath("images/email.gif"));
		sendMailBtn.disable();
		sendMailBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				Window.open("mailto:"+((Contact)contactBinding.getModel()).getMail(),
						"_blank", "");
			}
		});
		toolbar.add(sendMailBtn);
		
		contactBinding.addListener(Events.Bind, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				deleteContactBtn.enable();
				sendMailBtn.enable();
			}
		});
		
		contactBinding.addListener(Events.UnBind, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				deleteContactBtn.disable();
				sendMailBtn.disable();
			}
		});
		
		return toolbar;
	}
	
	public void bindCompany(Company company, Salesman contactSalesman) {
		this.company = company;
		contactsBox.setStore(emptyStore);
		loader.show();
		addContactBtn.enable();
		
		dataService.getContactsFor(contactSalesman, company,
			new AsyncCallback<ArrayList<Contact>>() {
				public void onSuccess(ArrayList<Contact> result) {
					contactStore = new ListStore<Contact>();
					contactStore.add(result);
					contactsBox.setStore(contactStore);
					contactsBox.setReadOnly(false);
					
					loader.hide();
				}

				public void onFailure(Throwable caught) {
					throw new RuntimeException(caught);
				}
			});
	}
	
	public void unbindCompany() {
		addContactBtn.disable();
		contactsBox.setStore(emptyStore);
		contactBinding.unbind();
		this.setReadOnly(true);
		contactsBox.clear();
		changeSalesman.disable();
	}
}
