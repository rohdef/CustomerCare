package as.markon.client;

import java.util.ArrayList;
import java.util.List;

import as.markon.viewmodel.MailContact;
import as.markon.viewmodel.Salesman;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.HtmlEditor;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class MailLayout extends LayoutContainer {
	private DataServiceAsync dataService;
	public MailLayout(final List<MailContact> recipients) {
		dataService = Global.getInstance().getDataService();
		this.setLayout(new FitLayout());

		FormPanel formPanel = new FormPanel();
		formPanel.setFrame(true);
		formPanel.setHeaderVisible(false);
		formPanel.setLayout(new FormLayout(LabelAlign.TOP));

		final TextField<String> subjectField = new TextField<String>();
		subjectField.setFieldLabel("Emne");

		final HtmlEditor contentEditor = new HtmlEditor();
		contentEditor.setFieldLabel("Indhold");
		contentEditor.setHeight(380);

		FormData formData = new FormData("100%");
		formPanel.add(subjectField, formData);
		formPanel.add(contentEditor, formData);

		formPanel.addButton(new Button("Send mail",
				new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						String subject = subjectField.getValue();
						String message = contentEditor.getValue();

						ArrayList<String> recipientMails = new ArrayList<String>();
						for (MailContact mc : recipients)
							recipientMails.add(mc.getMail());

						sendMail(0, subject, message, recipientMails);
					}
				}));

		formPanel.addButton(new Button("Anuller",
				new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						final Dialog confirmDialog = new Dialog();
						confirmDialog
								.setHeading("Vil du slette det du har skrevet?");
						confirmDialog.setModal(true);
						confirmDialog.setButtons(Dialog.YESNO);

						confirmDialog
								.addText("Er du sikker på, at du vil slette mailen?");
						confirmDialog.getButtonById(Dialog.YES).setText(
								"Slet mail");
						confirmDialog.getButtonById(Dialog.NO).setText(
								"Tilbage til mailen");

						confirmDialog.getButtonById(Dialog.YES)
								.addSelectionListener(
										new SelectionListener<ButtonEvent>() {
											@Override
											public void componentSelected(
													ButtonEvent ce) {
												fireEvent(Events.Close);
												confirmDialog.hide();
											}
										});

						confirmDialog.getButtonById(Dialog.NO)
								.addSelectionListener(
										new SelectionListener<ButtonEvent>() {
											@Override
											public void componentSelected(
													ButtonEvent ce) {
												confirmDialog.hide();
											}
										});

						confirmDialog.show();
					}
				}));

		this.add(formPanel);
	}

	private void sendMail(final int callCount, final String subject,
			final String message, final List<String> recipientMails) {
		if (callCount < 3) {
			Dialog askUserAndPassword = new Dialog();
			askUserAndPassword.setButtons(Dialog.OKCANCEL);
			askUserAndPassword.setHideOnButtonClick(true);
			askUserAndPassword.setModal(true);
			askUserAndPassword.setHeading("Hvem vil du sende som?");
			
			FormPanel loginForm = new FormPanel();
			loginForm.setAutoWidth(true);
			loginForm.setLayout(new FitLayout());
			loginForm.setHeaderVisible(false);
			
			final ListStore<Salesman> salesmanStore = new ListStore<Salesman>();
			final ComboBox<Salesman> salesmanBox = new ComboBox<Salesman>();
			
			Global.getInstance().getDataService().getSalesmen(new AsyncCallback<ArrayList<Salesman>>() {
				public void onSuccess(ArrayList<Salesman> result) {
					salesmanStore.add(result);
					salesmanBox.select(Global.getInstance().getCurrentSalesman());
				}
				
				public void onFailure(Throwable caught) {
					caught.printStackTrace();
				}
			});
			
			salesmanBox.setStore(salesmanStore);
			salesmanBox.setDisplayField("salesman");
			salesmanBox.setFieldLabel("Hvem vil du sende som?");
			salesmanBox.setTriggerAction(TriggerAction.ALL);
			salesmanBox.setAutoWidth(true);
			loginForm.add(salesmanBox);
			
			askUserAndPassword.getButtonById(Dialog.OK).setText("Send");
			askUserAndPassword.getButtonById(Dialog.OK).addSelectionListener(
					new SelectionListener<ButtonEvent>() {
						@Override
						public void componentSelected(ButtonEvent ce) {
							String sender = salesmanBox.getValue().getSalesman() +
								"<" + salesmanBox.getValue().getMail() + ">";
							dataService.sendMail(sender,
									subject, message, recipientMails,
									new AsyncCallback<Void>() {
								
								public void onSuccess(Void result) {
									fireEvent(Events.Close);
								}
								
								public void onFailure(Throwable caught) {
									sendMail((callCount+1), subject, message, recipientMails);
									caught.printStackTrace();
								}
							});
						}
			});
			
			askUserAndPassword.add(loginForm);
			askUserAndPassword.show();
		}
	}
}
