package dk.rohdef.client;

import java.util.ArrayList;


import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

import dk.rohdef.client.services.Global;
import dk.rohdef.client.specialtypes.VType;
import dk.rohdef.client.specialtypes.VTypeValidator;
import dk.rohdef.viewmodel.Trade;

public class TradeAdminDialog extends Dialog {
	private LoadingDialog loader = new LoadingDialog();
	private boolean loadingTrades = true;
	
	public TradeAdminDialog() {
		setModal(true);
		setHeading("Administrer brancher");
		setLayout(new RowLayout(Orientation.VERTICAL));
		this.setWidth(550);
		this.setHideOnButtonClick(true);
		GridCellRenderer<Trade> deleteBtnRenderer = new DeleteCellRenderer();
		
		// Grid
		final ListStore<Trade> tradeStore = new ListStore<Trade>();
		
		ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		
		ColumnConfig idColumn = new ColumnConfig("tradeid", "Id", 50);
		configs.add(idColumn);
		
		ColumnConfig tradeColumn = new ColumnConfig("trade", "Branche", 300);
		configs.add(tradeColumn);
		
		ColumnConfig deleteColumn = new ColumnConfig();
		deleteColumn.setId("remove");
		deleteColumn.setHeader("Slet branche");
		deleteColumn.setRenderer(deleteBtnRenderer);
		deleteColumn.setWidth(40);
		configs.add(deleteColumn);
		
		ColumnModel cm = new ColumnModel(configs);
		
		final Grid<Trade> tradeGrid = new Grid<Trade>(tradeStore, cm);
		tradeGrid.setBorders(false);
		tradeGrid.setColumnLines(true);
		tradeGrid.setColumnReordering(true);
		tradeGrid.setStripeRows(true);
		tradeGrid.setHeight(350);
		tradeGrid.getView().setEmptyText("Der er ingen bracher i listen.");
		
		this.add(tradeGrid);
		
		final FormPanel addTradePanel = new FormPanel();
		addTradePanel.setHeaderVisible(false);
		addTradePanel.setFrame(false);
		addTradePanel.setBorders(false);
		
		FormLayout addTradeLayout = new FormLayout(LabelAlign.TOP);
		addTradePanel.setLayout(addTradeLayout);
		
		final TextField<String> idFld = new TextField<String>();
		idFld.setFieldLabel("Brancheid");
		idFld.setValidator(new VTypeValidator(VType.NUMERIC));
		idFld.setAutoValidate(true);
		idFld.setAllowBlank(false);
		addTradePanel.add(idFld);
		
		final TextField<String> tradeFld = new TextField<String>();
		tradeFld.setFieldLabel("Branchenavn");
		tradeFld.setValidator(new VTypeValidator(VType.ALPHABET));
		tradeFld.setAllowBlank(false);
		tradeFld.setAutoValidate(true);
		addTradePanel.add(tradeFld);
		
		Button addBtn = new Button();
		addBtn.setText("Tilføj");
		addBtn.setType("Submit");
		addBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				final Trade t = new Trade();
				t.setTrade(tradeFld.getValue());
				t.set("tradeid", Integer.valueOf(idFld.getValue()));
				
				Global.getInstance().getDataService().addTrade(t, 
						new AsyncCallback<Void>() {
							public void onSuccess(Void result) {
								tradeStore.add(t);
							}
							
							public void onFailure(Throwable caught) {
							}
						});
				addTradePanel.clear();
			}
		});
		addTradePanel.add(addBtn);
		
		FormButtonBinding buttonBinding = new FormButtonBinding(addTradePanel);
		buttonBinding.addButton(addBtn);
		
		Global.getInstance().getDataService().getTrades(
			new AsyncCallback<ArrayList<Trade>>() {
				public void onSuccess(ArrayList<Trade> result) {
					for (Trade t : result)
						tradeStore.add(t);
					
					loadingTrades = false;
					checkLoader();
				}
			
				public void onFailure(Throwable caught) {
				}
		});
		
		this.add(addTradePanel);
	}
	
	@Override
	public void show() {
		super.show();
		checkLoader();
	}
	
	public void checkLoader() {
		if (loadingTrades)
			loader.show();
		else
			loader.hide();
	}
	
	private class DeleteCellRenderer implements GridCellRenderer<Trade> {
		public Object render(Trade model, String property, ColumnData config,
				int rowIndex, int colIndex, ListStore<Trade> store,
				Grid<Trade> grid) {
			Button deleteBtn = new Button("Slet");
			deleteBtn.setWidth(grid.getColumnModel().getColumnWidth(colIndex) - 10);
			
			deleteBtn.addSelectionListener(new DeleteListener(model, store));
			
			return deleteBtn;
		}
	}
	
	private class DeleteListener extends SelectionListener<ButtonEvent> {
		private Trade trade;
		private ListStore<Trade> store;
		
		public DeleteListener(Trade trade, ListStore<Trade> store) {
			this.trade = trade;
			this.store = store;
		}

		@Override
		public void componentSelected(ButtonEvent ce) {
			Dialog confirmDelete = new Dialog();
			confirmDelete.setButtons(Dialog.YESNO);
			confirmDelete.setHideOnButtonClick(true);
			confirmDelete.setHeading("Slet branchen "+trade.getTrade());
			confirmDelete.addText("Er du sikker på, at du vil slette"
				+ trade.getTrade() + "? Advarsel! Dette kan ikke fortrydes!");
			
			confirmDelete.getButtonById(Dialog.NO).setText("Behold");
			confirmDelete.getButtonById(Dialog.YES).setText("Slet branche");
			confirmDelete.getButtonById(Dialog.YES).addSelectionListener(
					new SelectionListener<ButtonEvent>() {
						@Override
						public void componentSelected(ButtonEvent ce) {
							Global.getInstance().getDataService().deleteTrade(trade,
									new AsyncCallback<Void>() {
								public void onSuccess(Void result) {
									store.remove(trade);
								}
								
								public void onFailure(Throwable caught) {
								}
							});
						}
					}
				);
			
			confirmDelete.show();
		}
	}
}
