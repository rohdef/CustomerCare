package as.markon.client;

import java.util.ArrayList;
import java.util.List;

import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;
import as.markon.viewmodel.MailContact;
import as.markon.viewmodel.Salesman;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class CustomerView extends LayoutContainer {
	private DataServiceAsync dataService = Global.getInstance()
			.getDataService();

	private CompanyListingPanel companyListing;

	public synchronized void setSalesman(Salesman salesman) {
		Global.getInstance().setCurrentSalesman(salesman);

		if (companyListing != null)
			companyListing.salesmanChanged();
	}

	public CustomerView(Salesman salesman) {
		this.setLayout(new BorderLayout());

		ContentPanel northPanel = createNorthPanel();
		companyListing = new CompanyListingPanel();
		ContentPanel eastPanel = createEastPanel();

		setSalesman(salesman);

		this.add(northPanel, new BorderLayoutData(LayoutRegion.NORTH, 100));
		this.add(companyListing,
				new BorderLayoutData(LayoutRegion.CENTER, 0.7f));
		this.add(eastPanel, new BorderLayoutData(LayoutRegion.EAST, 0.3f));
	}

	private ContentPanel createNorthPanel() {
		ContentPanel northPanel = new ContentPanel();
		HBoxLayout topLayout = new HBoxLayout();
		topLayout.setHBoxLayoutAlign(HBoxLayoutAlign.MIDDLE);
		northPanel.setLayout(topLayout);

		Text customersTxt = new Text();
		customersTxt.setText("Kunder");
		customersTxt.setStyleAttribute("font-size", "3em;");
		northPanel.add(customersTxt);

		HBoxLayoutData flex = new HBoxLayoutData(0, 15, 0, 0);
		northPanel.add(new Text(), flex);

		Button changeSalesmanBtn = new Button("Skift sælger",
				new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						final Dialog salesmanDialog = new Dialog();
						salesmanDialog.setButtons(Dialog.CANCEL);
						salesmanDialog
								.setHeading("Hvilken sælger ønsker du at se kartoteket for?");
						salesmanDialog.setHideOnButtonClick(true);

						Login l = new Login();
						l.addListener(Events.Select,
								new Listener<SelectionEvent<Salesman>>() {
									public void handleEvent(
											SelectionEvent<Salesman> be) {
										setSalesman(be.getModel());
										salesmanDialog.hide();
									}
								});

						salesmanDialog.add(l);
						salesmanDialog.show();
					}
				});

		northPanel.add(changeSalesmanBtn);
		return northPanel;
	}

	private ContentPanel createEastPanel() {
		ContentPanel eastPanel = new ContentPanel();
		eastPanel.setHeaderVisible(false);

		final CompanyEditPanel companyForm = new CompanyEditPanel();
		final FormPanel contactForm = createEastContactsForm();
		final ContentPanel mailForm = createEastMailForm();

		companyForm.setVisible(true);
		contactForm.setVisible(true);
		mailForm.setVisible(false);

		eastPanel.add(companyForm);
		eastPanel.add(contactForm);
		eastPanel.add(mailForm);

		// Show and hide areas appropiately
		companyListing
				.addSelectionListener(new Listener<SelectionChangedEvent<Company>>() {
					public void handleEvent(SelectionChangedEvent<Company> be) {
						if (be.getSelection().size() > 1) {
							companyForm.hide();
							contactForm.hide();
							mailForm.show();
						} else {
							companyForm.show();
							contactForm.show();
							mailForm.hide();
						}
					}
				});

		// Bind company form
		companyListing
				.addSelectionListener(new Listener<SelectionChangedEvent<Company>>() {
					public void handleEvent(SelectionChangedEvent<Company> be) {
						if (be.getSelection().size() == 1)
							companyForm.bindCompany(be.getSelectedItem());
						else
							companyForm.unbindCompany();
					}
				});

		return eastPanel;
	}

	private FormPanel createEastContactsForm() {
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

		CheckBox acceptsMailsBox = new CheckBox();
		acceptsMailsBox.setFieldLabel("Ønsker mails");
		acceptsMailsBox.setName("acceptsmails");
		contactsForm.add(acceptsMailsBox);

		TextArea commentFld = new TextArea();
		commentFld.setBorders(false);
		commentFld.setFieldLabel("Kommentarer");
		commentFld.setName("comments");
		contactsForm.add(commentFld);

		companyListing
				.addSelectionListener(new Listener<SelectionChangedEvent<Company>>() {
					public void handleEvent(SelectionChangedEvent<Company> be) {
						if (be.getSelection().size() > 0) {
							contactsBox.clear();

							contactsBox.setStore(emptyStore);

							dataService.getContactsFor(Global.getInstance()
									.getCurrentSalesman(),
									be.getSelectedItem(),
									new AsyncCallback<ArrayList<Contact>>() {

										public void onSuccess(
												ArrayList<Contact> result) {
											ListStore<Contact> contactStore = new ListStore<Contact>();
											contactStore.add(result);
											contactsBox.setStore(contactStore);
										}

										public void onFailure(Throwable caught) {
											krHandleError(caught);
										}
									});

						} else
							contactsBox.setStore(emptyStore);
					}
				});

		final FormBinding contactBinding = new FormBinding(contactsForm, true);

		contactsBox
				.addSelectionChangedListener(new SelectionChangedListener<Contact>() {
					@Override
					public void selectionChanged(
							SelectionChangedEvent<Contact> se) {
						contactBinding.bind(se.getSelectedItem());
					}
				});

		return contactsForm;
	}

	private ContentPanel createEastMailForm() {
		final ArrayList<ComboBox<MailContact>> boxes = new ArrayList<ComboBox<MailContact>>();

		ContentPanel mailForm = new ContentPanel();
		mailForm.setHeading("Mailindstillinger");

		GridCellRenderer<Company> recipientRenderer = new GridCellRenderer<Company>() {
			public Object render(Company model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<Company> store, Grid<Company> grid) {
				final ComboBox<MailContact> contactsBox = new ComboBox<MailContact>();
				contactsBox.setWidth(grid.getColumnModel().getColumnWidth(
						colIndex) - 10);
				contactsBox.setDisplayField("name");
				contactsBox.setTriggerAction(TriggerAction.ALL);
				contactsBox.setTypeAhead(true);
				contactsBox.setForceSelection(true);

				final ArrayList<MailContact> contactMails = new ArrayList<MailContact>();
				ListStore<MailContact> contactStore = new ListStore<MailContact>();
				contactStore.setMonitorChanges(true);

				if (model.getMail() != null && !model.getMail().isEmpty()
						&& model.getAcceptsMails())
					contactMails.add(new MailContact("Virksomheden", model
							.getMail()));

				dataService.getContactsFor(Global.getInstance()
						.getCurrentSalesman(), model,
						new AsyncCallback<ArrayList<Contact>>() {

							public void onSuccess(ArrayList<Contact> result) {
								ListStore<MailContact> contactStore = new ListStore<MailContact>();
								contactStore.setMonitorChanges(true);
								for (Contact c : result) {
									if (c.getMail() != null
											&& !c.getMail().isEmpty()
											&& c.getAcceptsMails())
										contactMails.add(new MailContact(c
												.getName(), c.getMail()));
								}
								contactStore.add(contactMails);
								contactsBox.setStore(contactStore);
							}

							public void onFailure(Throwable caught) {
								krHandleError(caught);
							}
						});

				contactsBox.setStore(contactStore);
				boxes.add(contactsBox);

				return contactsBox;
			}
		};

		GridCellRenderer<Company> removeBtnRenderer = new GridCellRenderer<Company>() {
			public Object render(Company model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<Company> store, Grid<Company> grid) {
				Button removeBtn = new Button("Fjern");
				final Company remModel = model;
				removeBtn
						.addSelectionListener(new SelectionListener<ButtonEvent>() {
							@Override
							public void componentSelected(ButtonEvent ce) {
								companyListing.deselectCompany(remModel);
							}
						});

				removeBtn.setWidth(grid.getColumnModel().getColumnWidth(
						colIndex) - 10);

				return removeBtn;
			}
		};

		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		ColumnConfig companyName = new ColumnConfig();
		companyName.setId("companyname");
		companyName.setHeader("Virksomhed");
		companyName.setWidth(190);

		ColumnConfig mailTo = new ColumnConfig();
		mailTo.setHeader("Modtager");
		mailTo.setRenderer(recipientRenderer);
		mailTo.setWidth(130);

		ColumnConfig removeBtnConfig = new ColumnConfig();
		removeBtnConfig.setId("remove");
		removeBtnConfig.setHeader("Fjern modtager");
		removeBtnConfig.setRenderer(removeBtnRenderer);
		removeBtnConfig.setWidth(40);

		configs.add(companyName);
		configs.add(mailTo);
		configs.add(removeBtnConfig);

		final ListStore<Company> selectedCompanies = new ListStore<Company>();
		companyListing
				.addSelectionListener(new Listener<SelectionChangedEvent<Company>>() {
					public void handleEvent(SelectionChangedEvent<Company> be) {
						selectedCompanies.removeAll();

						selectedCompanies.add(be.getSelection());
					}
				});

		ColumnModel cm = new ColumnModel(configs);

		final Grid<Company> mtGrid = new Grid<Company>(selectedCompanies, cm);
		mtGrid.setHeight(400);
		mtGrid.setBorders(false);
		mtGrid.setStripeRows(true);
		mailForm.add(mtGrid);

		mailForm.addButton(new Button("Skriv mail",
				new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						List<MailContact> recipients = new ArrayList<MailContact>();

						for (int i = 0; i < mtGrid.getStore().getCount(); i++) {
							@SuppressWarnings("unchecked")
							ComboBox<MailContact> combo = (ComboBox<MailContact>) mtGrid
									.getView().getWidget(i, 1);
							recipients.add(combo.getValue());
						}

						final Window mailWin = new Window();
						mailWin.setSize(700, 550);
						mailWin.setModal(true);
						mailWin.setHeading("Mail besked");
						mailWin.setLayout(new FitLayout());

						MailLayout mailLayout = new MailLayout(recipients);
						mailLayout.addListener(Events.Close,
								new Listener<BaseEvent>() {
									public void handleEvent(BaseEvent be) {
										mailWin.hide();
									}
								});

						mailWin.add(mailLayout, new FitData(4));

						mailWin.show();
					}
				}));

		mailForm.setWidth("100%");
		mailForm.setBorders(false);
		mailForm.setFrame(true);

		return mailForm;
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
