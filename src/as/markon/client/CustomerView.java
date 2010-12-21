package as.markon.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import as.markon.client.events.ContactEvent;
import as.markon.client.events.ContactListener;
import as.markon.client.events.DeleteCompanyEvent;
import as.markon.client.events.DeleteCompanyListener;
import as.markon.client.panels.CompanyEditPanel;
import as.markon.client.panels.CompanyListingPanel;
import as.markon.client.panels.ContactEditPanel;
import as.markon.client.panels.SelectLabelRecipiantsPanel;
import as.markon.client.panels.SelectMailRecipiantsPanel;
import as.markon.client.services.Global;
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
import com.google.gwt.user.client.ui.Image;

public class CustomerView extends LayoutContainer {
	private static Logger logger = Logger.getLogger(CustomerView.class.getName());
	
	private CompanyListingPanel companyListing;
	private CompanyEditPanel companyForm;
	private ContactEditPanel contactForm;
	private SelectMailRecipiantsPanel mailForm;
	private SelectLabelRecipiantsPanel labelForm;

	public CustomerView(Salesman salesman) {
		this.setLayout(new BorderLayout());

		setSalesman(salesman);
		
		ContentPanel northPanel = createNorthPanel();
		companyListing = new CompanyListingPanel();
		ContentPanel eastPanel = createEastPanel();

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

		Image profileImage = new Image("http://gravatar.com/avatar/"+
				Global.getInstance().getCurrentSalesman().getMailMd5());
		northPanel.add(profileImage);
		
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
						salesmanDialog.getButtonById(Dialog.CANCEL)
							.setText("Anuller");
						salesmanDialog
							.setHeading("Hvilken sælger ønsker du at se kartoteket for?");
						salesmanDialog.setHideOnButtonClick(true);
						salesmanDialog.setSize(600, 450);
						salesmanDialog.setScrollMode(Scroll.AUTO);
						salesmanDialog.setResizable(false);
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

		companyForm = new CompanyEditPanel();
		contactForm = new ContactEditPanel();
		mailForm = new SelectMailRecipiantsPanel();
		labelForm = new SelectLabelRecipiantsPanel();

		companyForm.setVisible(true);
		contactForm.setVisible(true);
		mailForm.setVisible(false);
		labelForm.setVisible(false);

		eastPanel.add(companyForm);
		eastPanel.add(contactForm);
		eastPanel.add(mailForm);
		eastPanel.add(labelForm);

		// Show and hide areas appropiately
		companyListing.addSelectionListener(
			new Listener<SelectionChangedEvent<Company>>() {
				public void handleEvent(SelectionChangedEvent<Company> be) {
					
					if (be.getSelection().size() > 1) {
						companyForm.hide();
						contactForm.hide();
						mailForm.show();
						labelForm.show();
					} else {
						companyForm.show();
						contactForm.show();
						mailForm.hide();
						labelForm.hide();
					}
				}
			});

		// Bind company and contacts form
		companyListing.addSelectionListener(new Listener<SelectionChangedEvent<Company>>() {
			public void handleEvent(SelectionChangedEvent<Company> be) {
				companyForm.unbindCompany();
				contactForm.unbindCompany();
				mailForm.unbindCompanies();
				labelForm.unbindCompanies();
				
				if (be.getSelection().size() == 1) {
					companyForm.bindCompany(be.getSelectedItem());
					
					Salesman contactSalesman = companyListing.isShowingProspects() ? null : 
						Global.getInstance().getCurrentSalesman();
					contactForm.bindCompany(be.getSelectedItem(), contactSalesman);
				} else if (be.getSelection().size() > 1) {
					mailForm.bindCompanies(be.getSelection());
					labelForm.bindCompanies(be.getSelection());
				}
			}
		});

		DeleteCompanyListener deleteListener = new DeleteCompanyListener() {
			public void handleEvent(DeleteCompanyEvent be) {
				companyListing.deselectCompany(be.getCompany());
			}
		};
		mailForm.addDeleteListener(deleteListener);
		labelForm.addDeleteListener(deleteListener);
		
		contactForm.addContactListener(new ContactEventListener());

		return eastPanel;
	}
	
	private class ContactEventListener implements ContactListener {
		public void handleEvent(ContactEvent be) {
			logger.log(Level.FINE, "ContactEvent happened: ");
			logger.log(Level.FINER, "\tContact: "+be.getContact());
			logger.log(Level.FINER, "\tOldContact: "+be.getOldContact());
			
			if (be.getType() == ContactEvent.DELETED_CONTACT_TYPE) {
				companyListing.removeContactFromLists(be.getContact());
				return;
			} else if (be.getType() == ContactEvent.NEW_CONTACT_TYPE) {
				companyListing.moveContactToCustomers(be.getContact());
				return;
			} else if (be.getType() == ContactEvent.CHANGED_CONTACT_TYPE) {
				if (be.getContact().getSalesman() == null) {
					if (be.getOldContact().getSalesman() != null) {
						companyListing.moveContactToProspects(be.getContact());
					}
				} else {
					if (be.getContact().getSalesman().
							equals(be.getOldContact().getSalesman())) {
						// Nothing changed really
					} else if (be.getContact().getSalesman().
							equals(Global.getInstance().getCurrentSalesman())) {
						companyListing.moveContactToCustomers(be.getContact());
					}
				}
				
				return;
			} else {
				logger.log(Level.WARNING, "This shouldn't be possible");
			}
		}
	}
}