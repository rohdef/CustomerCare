package dk.rohdef.client;

import java.util.ArrayList;
import java.util.List;


import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.IconHelper;
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

import dk.rohdef.client.services.DataServiceAsync;
import dk.rohdef.client.services.Global;
import dk.rohdef.viewmodel.Company;
import dk.rohdef.viewmodel.MailRecipient;
import dk.rohdef.viewmodel.Salesman;

public class MailLayout extends LayoutContainer {
	private DataServiceAsync dataService;
	private Company appCompany;
	
	public MailLayout(final List<MailRecipient> recipients) {
		dataService = Global.getInstance().getDataService();
		this.setLayout(new FitLayout());

		FormPanel formPanel = new FormPanel();
		formPanel.setFrame(true);
		formPanel.setHeaderVisible(false);
		formPanel.setLayout(new FormLayout(LabelAlign.TOP));

		final ListStore<Salesman> salespeopleStore = new ListStore<Salesman>();
		
		final ComboBox<Salesman> senderBox = new ComboBox<Salesman>();
		senderBox.setTriggerAction(TriggerAction.ALL);
		dataService.getSalesmen(new AsyncCallback<ArrayList<Salesman>>() {
			public void onSuccess(ArrayList<Salesman> result) {
				salespeopleStore.add(result);
				
				ArrayList<Salesman> selection = new ArrayList<Salesman>();
				selection.add(salespeopleStore.findModel(
						Global.getInstance().getCurrentSalesman()));
				senderBox.setSelection(selection);
			}
			
			public void onFailure(Throwable caught) {
			}
		});

		senderBox.setFieldLabel("Afsender");
		senderBox.setDisplayField("salesman");
		senderBox.setStore(salespeopleStore);
		
		final TextField<String> subjectField = new TextField<String>();
		subjectField.setFieldLabel("Emne");

		final HtmlEditor contentEditor = new HtmlEditor();
		contentEditor.setFieldLabel("Indhold");
		contentEditor.setHeight(380);
		
		dataService.getAppCompany(new AsyncCallback<Company>() {
			public void onSuccess(Company result) {
				appCompany = result;
				String initialValue = createInitialValue(
						Global.getInstance().getCurrentSalesman());
				contentEditor.setValue(initialValue);
			}
			
			public void onFailure(Throwable caught) {
			}
		});
		
		senderBox.addSelectionChangedListener(new SelectionChangedListener<Salesman>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<Salesman> se) {
				contentEditor.setValue(createInitialValue(se.getSelectedItem()));
			}
		});

		FormData formData = new FormData("100%");
		formPanel.add(senderBox, formData);
		formPanel.add(subjectField, formData);
		formPanel.add(contentEditor, formData);

		Button sendMailBtn = new Button("Send mail",
				new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				String subject = subjectField.getValue();
				String message = contentEditor.getValue();

				ArrayList<String> recipientMails = new ArrayList<String>();
				for (MailRecipient mc : recipients)
					recipientMails.add(mc.getMail());

				// FIXME should be reenabled later
				sendMail(senderBox.getSelection().get(0), subject, message, recipientMails);
			}
		});
		sendMailBtn.setIcon(IconHelper.createPath("images/email_go.gif"));
		formPanel.addButton(sendMailBtn);

		Button cancelBtn = new Button("Anuller",
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

				confirmDialog.setHideOnButtonClick(true);

				confirmDialog.show();
			}
		});
		cancelBtn.setIcon(IconHelper.createPath("images/cancel.gif"));
		formPanel.addButton(cancelBtn);

		this.add(formPanel);
	}

	private String createInitialValue(Salesman salesman) {
		String initialValue = "<html><body>";
		initialValue += getStyle();
		initialValue += "<br /><br />";
		initialValue += getSignature(salesman);
		initialValue += "</body></html>";
		return initialValue;
	}

	private void sendMail(Salesman salesman, String subject,
			String message, List<String> recipientMails) {
			
		String sender = salesman.getSalesman() + " <" + salesman.getMail() + ">";
		
		dataService.sendMail(sender, subject, message, recipientMails, 
			new AsyncCallback<Void>() {
				public void onSuccess(Void result) {
					fireEvent(Events.Close);
				}
						
				public void onFailure(Throwable caught) {
				}
			});
	}

	private String getStyle() {
		String css = "<style type=\"text/css\">";
		
		css += "#signature { background: #DEDEDE; }";
		css += "#signature address { margin-bottom: 0.8em; font-style: normal; }";
		
		css += "</style>";
		return css;
	}
	
	private String getSignature(Salesman salesman) {
		String signature = "<!-- SIGNATURE_START -->"
			+ "<div id=\"signature\"><address>"
			+ "Med venlig hilsen<br />"
			+ "{navn}<br />"
			+ "{titel}</address>"
			
			+ "<address>"
			+ "<strong>" + appCompany.getCompanyName() + "</strong><br />"
			+ appCompany.getAddress() + "<br />"
			+ appCompany.getCity() + "<br />"
			+ appCompany.get("country") + "<address>"
			
			+ "<address>Direct: " + appCompany.getPhone() + "<br />"
			+ "Mobile: {mobil}"
			+ "</address>"
			
			+ "<address>"
			+ "Company tel.: " + appCompany.getPhone() + "<br />"
			+ "Fax: " + appCompany.get("fax")
			+ "</address>"
			
			+ "<address>"
			+ "Web: <a href=\"" + appCompany.get("webpage") + "\">" +
				appCompany.get("webpage") + "</a><br />"
			+ "E-mail: <a href=\"mailto:{mail}\">{mail}</a><br />"
			+ "Vat No.:" + appCompany.get("vat-no")
			+ "</address>"
			
			+ "" // TODO Ignore vidste du for now
			
			+ "</div>"
			+ "<!-- SIGNATURE_END -->";
		
		signature = signature.replaceAll("\\{navn\\}", salesman.getSalesman());
		signature = signature.replaceAll("\\{mail\\}", salesman.getMail());
		signature = signature.replaceAll("\\{titel\\}", salesman.getTitle());

		if (salesman.getPhone() != null)
			signature = signature.replaceAll("\\{mobil\\}", salesman.getPhone());
//		signature = signature.replaceAll("\\{\\}", "");

		return signature;
	}
}
