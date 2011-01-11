package dk.rohdef.client.panels;

import java.util.ArrayList;


import com.extjs.gxt.ui.client.data.ChangeEvent;
import com.extjs.gxt.ui.client.data.ChangeListener;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.filters.StringFilter;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import dk.rohdef.client.CreateCompany;
import dk.rohdef.client.SalesmanAdminWindow;
import dk.rohdef.client.TradeAdminDialog;
import dk.rohdef.client.i18n.CustomerCareI18n;
import dk.rohdef.client.services.Global;

public class CompanyGridToolBar extends ToolBar {
	private CustomerCareI18n i18n;
	
	// Fields
	private TextField<String> searchFld;
	private Button newCompany;
	private Button deleteCompaniesBtn;
	private Button tradeAdminBtn;
	private Button salesmanAdminBtn;
	
	private ArrayList<ChangeListener> createCompanyListeners;
	private ArrayList<SelectionListener<ComponentEvent>> deleteCompanyListeners;
	
	public CompanyGridToolBar(final StringFilter filter, final boolean prospect) {
		i18n = Global.getInstance().getI18n();
		createCompanyListeners = new ArrayList<ChangeListener>();
		deleteCompanyListeners = new ArrayList<SelectionListener<ComponentEvent>>();
		
		this.setEnableOverflow(false);
		this.add(new LabelToolItem(i18n.search() + ": "));
		
		searchFld = new TextField<String>();
		searchFld.setWidth(100);
		searchFld.setEmptyText(Global.getInstance().getI18n().insertSearchStringHere());
		searchFld.addKeyListener(new KeyListener() {
			@Override
			public void handleEvent(ComponentEvent e) {
				super.handleEvent(e);
				filter.setValue(searchFld.getValue());
			}
		});
		this.add(searchFld);
		
		newCompany = new Button();
		newCompany.setText(i18n.createCompany());
		newCompany.setIcon(IconHelper.createPath("images/company_add.gif"));
		newCompany.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				final Window createCompanyWindow = new Window();
				final CreateCompany createCompany = new CreateCompany();
				createCompany.addListener(Events.Close, new Listener<BaseEvent>() {
					public void handleEvent(BaseEvent be) {
						createCompanyWindow.hide();
					}
				});
				
				createCompany.addChangeListener(new ChangeListener() {
					public void modelChanged(ChangeEvent event) {
						fireCreateCompanyListeners(event);
					}
				});
				
				createCompanyWindow.setLayout(new FitLayout());
				createCompanyWindow.add(createCompany);
				createCompanyWindow.setIcon(IconHelper.createPath("images/company_add.gif"));
				createCompanyWindow.setWidth(650);
				createCompanyWindow.setHeight(380);
				createCompanyWindow.setModal(true);
				createCompanyWindow.setHeading(i18n.createCompany());
				createCompanyWindow.show();
				createCompanyWindow.setAutoHeight(true);
			}
		});
		this.add(newCompany);
		
		deleteCompaniesBtn = new Button();
		deleteCompaniesBtn.setText(i18n.deleteSelectedCompanies());
		deleteCompaniesBtn.disable();
		deleteCompaniesBtn.setIcon(IconHelper.createPath("images/company_delete.gif"));
		deleteCompaniesBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				Dialog deleteDialog = new DeleteDialog(prospect);
				deleteDialog.setIcon(IconHelper.createPath("images/company_delete.gif"));
				deleteDialog.show();
			}
		});
		
		this.add(deleteCompaniesBtn);
		
		if (!prospect) {
			tradeAdminBtn = new Button();
			tradeAdminBtn.setText(i18n.manageTrades());
			tradeAdminBtn.setIcon(IconHelper.createPath("images/trades.gif"));
			tradeAdminBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					TradeAdminDialog dialog = new TradeAdminDialog();
					dialog.show();
				}
			});
			this.add(tradeAdminBtn);
			
			salesmanAdminBtn = new Button();
			salesmanAdminBtn.setText(i18n.manageSalespeople());
			salesmanAdminBtn.setIcon(IconHelper.createPath("images/salesmen.gif"));
			salesmanAdminBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce) {
					SalesmanAdminWindow window = new SalesmanAdminWindow();
					window.show();
				}
			});
			this.add(salesmanAdminBtn);
		}
	}
	
	/**
	 * Tell the component how many companies we have selected, so it can enable and 
	 * disable buttons accordingly.
	 * @param count
	 */
	public void setCompaniesSelected(int count) {
		if (count == 1 )
			deleteCompaniesBtn.enable();
		else
			deleteCompaniesBtn.disable();
	}
	
	public void addCreateCompanyListener(ChangeListener listener) {
		createCompanyListeners.add(listener);
	}
	
	public void removeCreateCompanyListener(ChangeListener listener) {
		createCompanyListeners.remove(listener);
	}
	
	private void fireCreateCompanyListeners(ChangeEvent event) {
		for (ChangeListener l : createCompanyListeners) {
			l.modelChanged(event);
		}
	}
	
	public void addDeleteCompanyListener(SelectionListener<ComponentEvent> listener) {
		deleteCompanyListeners.add(listener);
	}
	
	public void removeDeleteCompanyListener(SelectionListener<ComponentEvent> listener) {
		deleteCompanyListeners.remove(listener);
	}
	
	private void fireDeleteCompanyListeners(ComponentEvent event) {
		for (SelectionListener<ComponentEvent> l : deleteCompanyListeners) {
			l.componentSelected(event);
		}
	}
	
	private class DeleteDialog extends Dialog {
		public DeleteDialog(final boolean prospects) {
			this.setTitle(i18n.deleteCompanyDialogTitle());
			this.setHeading(i18n.deleteCompanyDialogTitle());
			this.addText(i18n.cannotBeUndone()); 
			this.addText(i18n.doYouWantToDelteTheCompany());
			this.setButtons(Dialog.YESNO);
			this.getButtonById(Dialog.YES).setText(i18n.deleteCompany());
			this.getButtonById(Dialog.YES).setIcon(IconHelper.createPath("images/company_delete.gif"));
			this.getButtonById(Dialog.NO).setText(i18n.cancel());
			this.getButtonById(Dialog.NO).setIcon(IconHelper.createPath("images/cancel_green.gif"));
			this.setHideOnButtonClick(true);
			
			this.getButtonById(Dialog.YES).addSelectionListener(
				new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						 fireDeleteCompanyListeners(ce);
					}
				});
		}
	}
}
