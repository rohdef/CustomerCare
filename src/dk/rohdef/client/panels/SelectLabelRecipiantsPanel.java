package dk.rohdef.client.panels;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.CheckBoxListView;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import dk.rohdef.client.events.DeleteCompanyEvent;
import dk.rohdef.client.events.DeleteCompanyListener;
import dk.rohdef.client.services.DataServiceAsync;
import dk.rohdef.client.services.Global;
import dk.rohdef.client.specialtypes.LabelRecipientCondition;
import dk.rohdef.client.specialtypes.XComboBox;
import dk.rohdef.client.specialtypes.LabelRecipientCondition.LabelCondition;
import dk.rohdef.viewmodel.Company;
import dk.rohdef.viewmodel.Contact;
import dk.rohdef.viewmodel.LabelRecipient;

/**
 * Creates a grid showing companies and contacts, with the corrosponding comboboxes for 
 * selecting recipients and delete buttons to remove them again.
 * @author Rohde Fischer <rohdef@rohdef.dk>
 */
public class SelectLabelRecipiantsPanel extends FormPanel {
	private String headingTextStart = "Labelindstillinger ("
		, headingTextEnd = " labels valgt)";
	private int selectionCount;
	
	private static Logger logger = Logger.getLogger(SelectLabelRecipiantsPanel.class.getName());
	private DataServiceAsync dataService = Global.getInstance().getDataService();
	private ArrayList<ComboBox<LabelRecipient>> boxes;
	private Grid<Company> mtGrid;
	private ListStore<Company> selectedCompanies, emptyStore;
	private ArrayList<DeleteCompanyListener> deleteListeners;

	/**
	 * 
	 */
	public SelectLabelRecipiantsPanel() {
		boxes = new ArrayList<ComboBox<LabelRecipient>>();
		deleteListeners = new ArrayList<DeleteCompanyListener>();

		selectionCount = 0;
		this.setHeading(headingTextStart + selectionCount + headingTextEnd);
		
		ColumnModel cm = getColumnModel();

		mtGrid = new Grid<Company>(emptyStore, cm);
		mtGrid.setHeight(200);
		mtGrid.setBorders(false);
		mtGrid.setStripeRows(true);
		
		this.add(mtGrid);
		this.setTopComponent(createToolBar());
		
		this.setWidth("100%");
		this.setBorders(false);
		this.setFrame(true);
	}
	
	/**
	 * This will fetch the selected recipients, and send them to the backend where they 
	 * are stored for pdf rendering. When the recipients are stored this will open a new 
	 * window that calls the file rendering the pdf from the stored data and offers it 
	 * for download.
	 */
	private void createPdfForPrint() {
		logger.log(Level.INFO, "Starting pdf call");
		
		ArrayList<LabelRecipient> recipients = new ArrayList<LabelRecipient>();

		// Create the list of selected recipients
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
	
	/**
	 * Get the column model for the grid. This model has three columns. The company name, 
	 * the list of available recipients and the remove button.
	 * @return
	 */
	private ColumnModel getColumnModel() {
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
		removeBtnConfig.setWidth(80);

		configs.add(companyName);
		configs.add(mailTo);
		configs.add(removeBtnConfig);

		selectedCompanies = new ListStore<Company>();
		emptyStore = new ListStore<Company>();

		ColumnModel cm = new ColumnModel(configs);
		
		return cm;
	}
	
	/**
	 * Create the toolbar for the desired options.
	 * @return
	 */
	private ToolBar createToolBar() {
		ToolBar toolBar = new ToolBar();
		toolBar.setEnableOverflow(false);
		
		Button printBtn = new Button("Print labels", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				createPdfForPrint();
			}
		});
		toolBar.add(printBtn);
		printBtn.setIcon(IconHelper.createPath("images/printer.gif"));
		
		Button selectionMenuBtn = new Button();
		selectionMenuBtn.setText("Vælg modtagere");
		selectionMenuBtn.setIcon(IconHelper.createPath("images/select_recipients.gif"));
		
		Menu selectionMenu = new Menu();
		
		MenuItem selectAll = new MenuItem();
		selectAll.setText("Vælg alle");
		selectAll.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				selectByCondition(new LabelRecipientCondition(LabelCondition.ALL));
			}
		});
		selectionMenu.add(selectAll);
		
		MenuItem selectContacts = new MenuItem();
		selectContacts.setText("Vælg kontakterne (att.)");
		selectContacts.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				selectByCondition(new LabelRecipientCondition(LabelCondition.CONTACT));
			}
		});
		selectionMenu.add(selectContacts);
		
		MenuItem selectCompanies = new MenuItem();
		selectCompanies.setText("Vælg virksomehederne (ikke att.)");
		selectCompanies.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				selectByCondition(new LabelRecipientCondition(LabelCondition.COMPANY));
			}
		});
		selectionMenu.add(selectCompanies);
		
		selectionMenuBtn.setMenu(selectionMenu);
		toolBar.add(selectionMenuBtn);
		
		return toolBar;
	}
	
	/**
	 * Select all companies by the condition specified in {@link LabelRecipientCondition}.
	 * @param condition
	 */
	public void selectByCondition(LabelRecipientCondition condition) {
		for (int i = 0; i < mtGrid.getStore().getCount(); i++) {
			@SuppressWarnings("unchecked")
			XComboBox<LabelRecipient> combo = (XComboBox<LabelRecipient>) mtGrid
					.getView().getWidget(i, 1);
			
			for (LabelRecipient r : combo.getStore().getModels()) {
				if (condition.includeThis(r)) {
					CheckBoxListView<LabelRecipient> cblv = 
						(CheckBoxListView<LabelRecipient>) combo.getView();
					if (!cblv.isRendered())
						cblv.render(combo.getElement());
					
					(cblv).setChecked(r, true);
				}
			}
			combo.collapse();
		}
		recountSelection();
	}
	
	/**
	 * Sets the list of companies to show in the grid. This clears the previous selection.
	 * @param companies
	 */
	public void bindCompanies(List<Company> companies) {
		selectionCount = 0;
		this.setHeading(headingTextStart + selectionCount + headingTextEnd);
		
		selectedCompanies = new ListStore<Company>();
		selectedCompanies.add(companies);
		mtGrid.reconfigure(selectedCompanies, mtGrid.getColumnModel());
	}
	
	/**
	 * Empty the grid from companies and clear the selections.
	 */
	public void unbindCompanies() {
		mtGrid.reconfigure(emptyStore, mtGrid.getColumnModel());
		
		selectionCount = 0;
		this.setHeading(headingTextStart + selectionCount + headingTextEnd);
	}
	
	/**
	 * Updates the selection count in the heading.
	 */
	private void recountSelection() {
		selectionCount = 0;
		
		for (int i = 0; i < mtGrid.getStore().getCount(); i++) {
			@SuppressWarnings("unchecked")
			XComboBox<LabelRecipient> combo = (XComboBox<LabelRecipient>) mtGrid
					.getView().getWidget(i, 1);
			selectionCount += combo.getSelection().size();
		}
		
		this.setHeading(headingTextStart + selectionCount + headingTextEnd);
	}
	
	/**
	 * Add a delete listener that listens for when a company is removed from the grid of 
	 * companies.
	 * @param listener
	 */
	public void addDeleteListener(DeleteCompanyListener listener) {
		deleteListeners.add(listener);
	}

	/**
	 * Remove a delete listener that listens for when a company is removed from the grid 
	 * of companies.
	 * @param listener
	 */
	public void removeDeleteListener(DeleteCompanyListener listener) {
		deleteListeners.remove(listener);
	}
	
	/**
	 * A custom cell renderer to show the comboboxes in the grid correctly. This basically 
	 * do some custom analysis of the model and render a combobox.
	 * @author Rohde Fischer <rohdef@rohdef.dk>
	 */
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
			contactsBox.addListener(Events.Collapse, new Listener<BaseEvent>() {
				public void handleEvent(BaseEvent be) {
					recountSelection();
				}
			});
			
			boxes.add(contactsBox);

			return contactsBox;
		}
	}

	/**
	 * Renders cells with a delete button in it. 
	 * @author Rohde Fischer <rohdef@rohdef.dk>
	 */
	private class RemoveCellRenderer implements GridCellRenderer<Company> {
		public Object render(final Company model, String property, ColumnData config,
				int rowIndex, int colIndex, ListStore<Company> store, Grid<Company> grid) {
			Button removeBtn = new Button("Fjern");
			removeBtn.setIcon(IconHelper.createPath("images/delete.gif"));
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
}
