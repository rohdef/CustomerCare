package as.markon.client;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;
import as.markon.viewmodel.LabelRecipient;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SelectLabelRecipiantsPanel extends FormPanel {
	private static Logger logger = Logger.getLogger(SelectLabelRecipiantsPanel.class.getName());
	private DataServiceAsync dataService = Global.getInstance().getDataService();
	private ArrayList<ComboBox<LabelRecipient>> boxes;
	private Grid<Company> mtGrid;
	private ListStore<Company> selectedCompanies, emptyStore;
	private ColumnModel cm;
	private ArrayList<DeleteCompanyListener> deleteListeners;

	public SelectLabelRecipiantsPanel() {
		boxes = new ArrayList<ComboBox<LabelRecipient>>();
		deleteListeners = new ArrayList<DeleteCompanyListener>();

		this.setHeading("Labelindstillinger");
		
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

		this.addButton(new Button("Print labels", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				logger.log(Level.INFO, "Starting pdf call");
				
				ArrayList<LabelRecipient> recipients = new ArrayList<LabelRecipient>();

				for (int i = 0; i < mtGrid.getStore().getCount(); i++) {
					@SuppressWarnings("unchecked")
					XComboBox<LabelRecipient> combo = (XComboBox<LabelRecipient>) mtGrid
							.getView().getWidget(i, 1);
					List<LabelRecipient> selectedRecipients = combo.getSelection();
					for (LabelRecipient recipient : selectedRecipients) {
						if (recipient == null) {
							logger.log(Level.FINER, "Recipient is null, so none is" +
									"selected, ignoring entry.");
						} else {
							logger.log(Level.FINE, "Adding " + recipient.getName());
							recipients.add(recipient);
						}
					}
				}
				
				logger.log(Level.INFO, recipients.size() + " label recipients recorded");
				
				dataService.createPdf(recipients,
						new AsyncCallback<Integer>() {
							public void onSuccess(Integer result) {
								String url = "./customercare/pdfdownload?labelsessid="
									+result;
								logger.log(Level.FINE, "Accessubg url "+url);
								Window.open(url, "_blank", "");
							}
							
							public void onFailure(Throwable caught) {
								logger.log(Level.SEVERE,
										"Creating the pdf model failed",
										caught);
							}
						});
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
			final XComboBox<LabelRecipient> contactsBox = new XComboBox<LabelRecipient>();
			contactsBox.setWidth(grid.getColumnModel().getColumnWidth(colIndex) - 10);
			contactsBox.setDisplayField("name");
			contactsBox.setTriggerAction(TriggerAction.ALL);
			contactsBox.setTypeAhead(true);

			final ArrayList<LabelRecipient> contactMails = new ArrayList<LabelRecipient>();
			ListStore<LabelRecipient> contactStore = new ListStore<LabelRecipient>();
			contactStore.setMonitorChanges(true);

			contactMails.add(new LabelRecipient("Virksomheden", model));

			dataService.getContactsFor(Global.getInstance()
					.getCurrentSalesman(), model,
					new AsyncCallback<ArrayList<Contact>>() {

						public void onSuccess(ArrayList<Contact> result) {
							ListStore<LabelRecipient> contactStore = new ListStore<LabelRecipient>();
							contactStore.setMonitorChanges(true);
							for (Contact c : result) {
								contactMails.add(new LabelRecipient(c.getName(), c));
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
}
