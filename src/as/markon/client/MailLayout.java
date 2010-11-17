package as.markon.client;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.HtmlEditor;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

public class MailLayout extends LayoutContainer {

	public MailLayout() {
		this.setLayout(new FitLayout());
		
		FormPanel formPanel = new FormPanel();
		formPanel.setFrame(true);
		formPanel.setHeaderVisible(false);
		formPanel.setLayout(new FormLayout(LabelAlign.TOP));
		
		TextField<String> subjectField = new TextField<String>();
		subjectField.setFieldLabel("Emne");
		
		HtmlEditor contentEditor = new HtmlEditor();
		contentEditor.setFieldLabel("Indhold");
		contentEditor.setHeight(380);
		
		FormData formData = new FormData("100%");
		formPanel.add(subjectField, formData);
		formPanel.add(contentEditor, formData);
		
		formPanel.addButton(new Button("Send mail", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				// TODO Attach mail data
				fireEvent(Events.Complete);
			}
		}));
		
		formPanel.addButton(new Button("Anuller", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				final Dialog confirmDialog = new Dialog();
				confirmDialog.setHeading("Vil du slette det du har skrevet?");
				confirmDialog.setModal(true);
				confirmDialog.setButtons(Dialog.YESNO);
				
				confirmDialog.addText("Er du sikker på, at du vil slette mailen?");				
				confirmDialog.getButtonById(Dialog.YES).setText("Slet mail");
				confirmDialog.getButtonById(Dialog.NO).setText("Tilbage til mailen");
				
				confirmDialog.getButtonById(Dialog.YES)
					.addSelectionListener(new SelectionListener<ButtonEvent>() {
						@Override
						public void componentSelected(ButtonEvent ce) {
							fireEvent(Events.Close);
							confirmDialog.hide();
						}
					});
				
				confirmDialog.getButtonById(Dialog.NO)
					.addSelectionListener(new SelectionListener<ButtonEvent>() {
						@Override
						public void componentSelected(ButtonEvent ce) {
							confirmDialog.hide();
						}
					});
				
				confirmDialog.show();
			}
		}));
		
		this.add(formPanel);
	}

}
