package as.markon.client;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;
import as.markon.viewmodel.MailRecipient;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SelectMailRecipiantsPanel extends FormPanel {
	private static Logger logger = Logger.getLogger(SelectMailRecipiantsPanel.class.getName());
	private DataServiceAsync dataService = Global.getInstance().getDataService();
	private ArrayList<ComboBox<MailRecipient>> boxes;
	private Grid<Company> mtGrid;
	private ListStore<Company> selectedCompanies, emptyStore;
	private ColumnModel cm;
	private ArrayList<DeleteCompanyListener> deleteListeners;

	public SelectMailRecipiantsPanel() {
		boxes = new ArrayList<ComboBox<MailRecipient>>();
		deleteListeners = new ArrayList<DeleteCompanyListener>();

		this.setHeading("Mailindstillinger");
		
		GridCellRenderer<Company> recipientRenderer = new RecipientCellRenderer();

		GridCellRenderer<Company> removeBtnRenderer = new RemoveCellRenderer();

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

		selectedCompanies = new ListStore<Company>();
		emptyStore = new ListStore<Company>();
					

		cm = new ColumnModel(configs);

		mtGrid = new Grid<Company>(emptyStore, cm);
		mtGrid.setHeight(200);
		mtGrid.setBorders(false);
		mtGrid.setStripeRows(true);
		this.add(mtGrid);
		
		this.addButton(new Button("Skriv mail",
				new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						logger.log(Level.INFO, "Preparing mail call");
						List<MailRecipient> recipients = new ArrayList<MailRecipient>();

						for (int i = 0; i < mtGrid.getStore().getCount(); i++) {
							@SuppressWarnings("unchecked")
							XComboBox<MailRecipient> combo = (XComboBox<MailRecipient>) mtGrid
									.getView().getWidget(i, 1);
							
							List<MailRecipient> selectedRecipients = combo.getSelection();
							for (MailRecipient recipient : selectedRecipients) {
								if (recipient == null)
									logger.log(Level.FINER, "\tIgnoring company, " +
											"contact not set.");
								else {
									logger.log(Level.FINER, "\tAdding recipient: " + 
											recipient.getName() + 
											"<" + recipient.getMail() + ">");
									recipients.add(recipient);
								}
							}
						}
						logger.log(Level.INFO, recipients.size() + " mail recipients recorded");

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

		this.setWidth("100%");
		this.setBorders(false);
		this.setFrame(true);
	}
	
	public void bindCompanies(List<Company> companies) {
		selectedCompanies = new ListStore<Company>();
		selectedCompanies.add(companies);
		mtGrid.reconfigure(selectedCompanies, cm);
	}
	
	public void unbindCompanies() {
		mtGrid.reconfigure(emptyStore, cm);
	}
	
	private class RecipientCellRenderer implements GridCellRenderer<Company> {
		public Object render(Company model, String property,
				ColumnData config, int rowIndex, int colIndex,
				ListStore<Company> store, Grid<Company> grid) {
			final XComboBox<MailRecipient> contactsBox = new XComboBox<MailRecipient>();
			contactsBox.setWidth(grid.getColumnModel().getColumnWidth(
					colIndex) - 10);
			contactsBox.setDisplayField("name");
			contactsBox.setTriggerAction(TriggerAction.ALL);
			contactsBox.setTypeAhead(true);

			final ArrayList<MailRecipient> contactMails = new ArrayList<MailRecipient>();
			ListStore<MailRecipient> contactStore = new ListStore<MailRecipient>();
			contactStore.setMonitorChanges(true);

			dataService.getContactsFor(Global.getInstance()
					.getCurrentSalesman(), model,
					new AsyncCallback<ArrayList<Contact>>() {

						public void onSuccess(ArrayList<Contact> result) {
							ListStore<MailRecipient> contactStore = new ListStore<MailRecipient>();
							contactStore.setMonitorChanges(true);
							for (Contact c : result) {
								if (c.getMail() != null
										&& !c.getMail().isEmpty()
										&& c.getAcceptsMails())
									contactMails.add(new MailRecipient(c
											.getName(), c.getMail()));
							}
							contactStore.add(contactMails);
							contactsBox.setStore(contactStore);
						}

						public void onFailure(Throwable caught) {
							throw new RuntimeException(caught);
						}
					});

			contactsBox.setStore(contactStore);
			boxes.add(contactsBox);

			return contactsBox;
		}
	}

	private class RemoveCellRenderer implements GridCellRenderer<Company> {
		public Object render(final Company model, String property, ColumnData config,
				int rowIndex, int colIndex, ListStore<Company> store, Grid<Company> grid) {
			Button removeBtn = new Button("Fjern");
			removeBtn.setWidth(grid.getColumnModel().getColumnWidth(colIndex) - 10);

			removeBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					for (DeleteCompanyListener l : deleteListeners)
						l.handleEvent(new DeleteCompanyEvent(new EventType(), model));
				}
			});

			return removeBtn;
		}
	}
	
	public void addDeleteListener(DeleteCompanyListener listener) {
		deleteListeners.add(listener);
	}
	
	public void removeDeleteListener(DeleteCompanyListener listener) {
		deleteListeners.remove(listener);
	}
	
	private static native void openUrl(String url, String name) /*-{
		$wnd.open(url, name);
	}-*/;
}
