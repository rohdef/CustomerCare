package as.markon.client.panels;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import as.markon.client.CreateContactWindow;
import as.markon.client.LoadingDialog;
import as.markon.client.events.ContactEvent;
import as.markon.client.events.ContactListener;
import as.markon.client.services.DataServiceAsync;
import as.markon.client.services.Global;
import as.markon.client.specialtypes.VType;
import as.markon.client.specialtypes.VTypeValidator;
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
	private static Logger logger = Logger.getLogger(ContactEditPanel.class.getName());
	
	private ArrayList<ContactListener> contactListeners; 
	private LoadingDialog loader = new LoadingDialog();
	private Button changeSalesman;
	private Company company;
	private Button addContactBtn;
	private Button saveBtn;

	public ContactEditPanel() {
		this.setHeading("Kontakter");

		contactListeners = new ArrayList<ContactListener>();
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
	
	/**
	 * Saves the contact and updates the selection in the combobox for this panel.
	 */
	private void save() {
		Contact contact = (Contact) contactBinding.getModel();
		
		dataService.updateContact(contact, new AsyncCallback<Void>() {
			public void onSuccess(Void result) {
				contactsBox.setValue((Contact) contactBinding.getModel());
			}
			
			public void onFailure(Throwable caught) {
				throw new RuntimeException(caught);
			}
		});
	}
	
	/**
	 * Get the toolbar for this panel.
	 * @return
	 */
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
						salesmanBox.setTriggerAction(TriggerAction.ALL);
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
								final Contact contact = (Contact)contactBinding.getModel();
								final Contact oldContact = new Contact();
								oldContact.setProperties(contact.getProperties());
								Salesman salesman = salesmanBox.getValue();
								contact.setSalesman(salesman);
								dataService.updateContact(contact, new AsyncCallback<Void>() {
									public void onSuccess(Void result) {
										fireContactEvent(new ContactEvent(
												ContactEvent.CHANGED_CONTACT_TYPE,
												contact, oldContact)
										);
									}
									
									public void onFailure(Throwable caught) {
									}
								});
								
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
				
				createContact.addContactListener(new ContactListener() {
					public void handleEvent(ContactEvent be) {
						ContactEvent event = be;
						if (company != null) {
							event = new ContactEvent(be.getType(), be.getContact(),
									null, company);
						}
						
						fireContactEvent(event);
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
												Contact contact = (Contact)contactBinding
													.getModel();
												contactStore.remove(contact);
												contactBinding.unbind();
												
												fireContactEvent(
														new ContactEvent(ContactEvent.
																DELETED_CONTACT_TYPE,
																contact,
																null,
																company));
												
												if (contactStore.getCount() > 0)
													contactsBox.setValue(
															contactStore.getAt(0));
												else
													contactsBox.clear();
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
	
	/**
	 * Bind company and salesman to the panel to show the contacts for the selected 
	 * company and salesman.
	 * 
	 * This should be called when a company is selected or when the selection is changes. 
	 * There is no need to run unbindCompany first when changing selection. This is 
	 * sufficient.
	 * @param company the newly selected company.
	 * @param contactSalesman the salesman to show contacts for. This will most likely 
	 * be the current salesman.
	 */
	public void bindCompany(Company company, Salesman contactSalesman) {
		this.company = company;
		contactsBox.setStore(emptyStore);
		loader.show();
		addContactBtn.enable();
		
		logger.log(Level.INFO, "Fetching contacts for " + company.getCompanyName());
		logger.log(Level.INFO, "Salesman is set to null? " + (contactSalesman == null));
		dataService.getContactsFor(contactSalesman, company,
			new AsyncCallback<ArrayList<Contact>>() {
				public void onSuccess(ArrayList<Contact> result) {
					contactStore = new ListStore<Contact>();
					contactStore.add(result);
					contactsBox.setStore(contactStore);
					contactsBox.setReadOnly(false);
					if (result.size() > 0)
						contactsBox.setValue(result.get(0));
					
					loader.hide();
				}

				public void onFailure(Throwable caught) {
					throw new RuntimeException(caught);
				}
			});
	}
	
	/**
	 * Removes the company binding for this panel. Use this to clear the boxes and make 
	 * the panel read only. This should be used when no company is selected any more.
	 */
	public void unbindCompany() {
		addContactBtn.disable();
		contactsBox.setStore(emptyStore);
		contactBinding.unbind();
		this.setReadOnly(true);
		contactsBox.clear();
		changeSalesman.disable();
	}
	
	/**
	 * Register a ContactListener to handle changes in the contact.
	 * @param l the listener handling the changes in the contact.
	 */
	public void addContactListener(ContactListener l) {
		contactListeners.add(l);
	}
	
	/**
	 * Remove a ContatListener from the registered listeners.
	 * @param l the listener to remove.
	 */
	public void removeContactListene(ContactListener l) {
		contactListeners.remove(l);
	}
	
	/**
	 * Fire the registered ContactListeners when the containing event.
	 * @param event contains the details of the change.
	 */
	private void fireContactEvent(ContactEvent event) {
		for (ContactListener l : contactListeners) {
			l.handleEvent(event);
		}
	}
}