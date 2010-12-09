package as.markon.client;

import as.markon.viewmodel.Company;
import as.markon.viewmodel.Salesman;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;

public class CustomerView extends LayoutContainer {
	private CompanyListingPanel companyListing;

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

	public synchronized void setSalesman(Salesman salesman) {
		Global.getInstance().setCurrentSalesman(salesman);

		if (companyListing != null)
			companyListing.salesmanChanged();
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

		Button changeSalesmanBtn = new Button("Skift logon",
				new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						final Dialog salesmanDialog = new Dialog();
						salesmanDialog.setButtons(Dialog.CANCEL);
						salesmanDialog
							.setHeading("Hvilken sælger ønsker du at se kartoteket for?");
						salesmanDialog.setHideOnButtonClick(true);
						salesmanDialog.setSize(400, 300);
						salesmanDialog.setScrollMode(Scroll.AUTO);
						Login l = new Login();
						l.setLayout(new FitLayout());
						
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
						l.setHeight(salesmanDialog.getInnerHeight());
					}
				});

		northPanel.add(changeSalesmanBtn);
		return northPanel;
	}

	private ContentPanel createEastPanel() {
		ContentPanel eastPanel = new ContentPanel();
		eastPanel.setHeaderVisible(false);

		final CompanyEditPanel companyForm = new CompanyEditPanel();
		final ContactEditPanel contactForm = new ContactEditPanel();
		final SelectMailRecipiantsPanel mailForm = new SelectMailRecipiantsPanel();

		companyForm.setVisible(true);
		contactForm.setVisible(true);
		mailForm.setVisible(false);

		eastPanel.add(companyForm);
		eastPanel.add(contactForm);
		eastPanel.add(mailForm);

		// Show and hide areas appropiately
		companyListing.addSelectionListener(
			new Listener<SelectionChangedEvent<Company>>() {
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

		// Bind company and contacts form
		companyListing.addSelectionListener(new Listener<SelectionChangedEvent<Company>>() {
			public void handleEvent(SelectionChangedEvent<Company> be) {
				companyForm.unbindCompany();
				contactForm.unbindCompany();
				mailForm.unbindCompanies();
				
				if (be.getSelection().size() == 1) {
					companyForm.bindCompany(be.getSelectedItem());
					
					Salesman contactSalesman = companyListing.isShowingProspects() ? null : 
						Global.getInstance().getCurrentSalesman();
					contactForm.bindCompany(be.getSelectedItem(), contactSalesman);
				} else if (be.getSelection().size() > 1) {
					mailForm.bindCompanies(be.getSelection());
				}
			}
		});
	
		mailForm.addDeleteListener(new DeleteCompanyListener() {
			public void handleEvent(DeleteCompanyEvent be) {
				companyListing.deselectCompany(be.getCompany());
			}
		});

		return eastPanel;
	}

//	private void krHandleError(Throwable t) {
//		Dialog errorMessage = new Dialog();
//		errorMessage.setTitle("Fejl i systemet");
//		errorMessage.setButtons(Dialog.OK);
//		errorMessage.setBodyStyle("pad-text");
//		errorMessage.setScrollMode(Scroll.AUTO);
//		errorMessage.setHideOnButtonClick(true);
//		errorMessage.addText("Der er desværre sket en fejl :(\n\""
//				+ "\"\nsystemet skulle gerne have lavet en log\n"
//				+ "til ham den flinke på hjul.");
//
//		errorMessage.show();
//	}
}