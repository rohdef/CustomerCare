package dk.rohdef.client;

import java.util.logging.Level;
import java.util.logging.Logger;


import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Padding;
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

import dk.rohdef.client.events.ContactEvent;
import dk.rohdef.client.events.ContactListener;
import dk.rohdef.client.events.DeleteCompanyEvent;
import dk.rohdef.client.events.DeleteCompanyListener;
import dk.rohdef.client.panels.CompanyEditPanel;
import dk.rohdef.client.panels.CompanyListingPanel;
import dk.rohdef.client.panels.ContactEditPanel;
import dk.rohdef.client.panels.SelectLabelRecipiantsPanel;
import dk.rohdef.client.panels.SelectMailRecipiantsPanel;
import dk.rohdef.client.services.Global;
import dk.rohdef.viewmodel.Company;
import dk.rohdef.viewmodel.Salesman;

/**
 * This is the primary view handling what panels to show and where to show them.
 * @author Rohde Fischer <rohdef@rohdef.dk>
 */
public class CustomerView extends LayoutContainer {
	private static Logger logger = Logger.getLogger(CustomerView.class.getName());
	
	private CompanyListingPanel companyListing;
	private CompanyEditPanel companyForm;
	private ContactEditPanel contactForm;
	private SelectMailRecipiantsPanel mailForm;
	private SelectLabelRecipiantsPanel labelForm;

	private Image profileImage;

	/**
	 * Creates a new instance of the view. This needs a salesman to fetch the correct 
	 * companies.
	 * @param salesman
	 */
	public CustomerView(Salesman salesman) {
		this.setLayout(new BorderLayout());

		ContentPanel northPanel = createNorthPanel();
		companyListing = new CompanyListingPanel();
		ContentPanel eastPanel = createEastPanel();

		setSalesman(salesman);

		this.add(northPanel, new BorderLayoutData(LayoutRegion.NORTH, 120));
		this.add(companyListing,
				new BorderLayoutData(LayoutRegion.CENTER, 0.7f));
		this.add(eastPanel, new BorderLayoutData(LayoutRegion.EAST, 0.3f));
	}

	/**
	 * Set the salesman to update the list of companies and set the profile picture. 
	 * this also registers the global salesman, so other parts of the application can 
	 * see who is currently show.
	 * @param salesman
	 */
	public synchronized void setSalesman(Salesman salesman) {
		Global.getInstance().setCurrentSalesman(salesman);
		profileImage.setUrl("http://gravatar.com/avatar/" + salesman.getMailMd5());
		profileImage.setAltText(salesman.getSalesman());
		
		if (companyListing != null)
			companyListing.salesmanChanged();
	}
	
	/**
	 * Create the north panel showing the profile picture and the link to change salesman.
	 * @return
	 */
	private ContentPanel createNorthPanel() {
		ContentPanel northPanel = new ContentPanel();
		HBoxLayout topLayout = new HBoxLayout();
		topLayout.setHBoxLayoutAlign(HBoxLayoutAlign.MIDDLE);
		topLayout.setPadding(new Padding(5));
		northPanel.setLayout(topLayout);

		profileImage = new Image();
			
//			new Image("http://gravatar.com/avatar/"+
//				Global.getInstance().getCurrentSalesman().getMailMd5());
		profileImage.setWidth("80px");
		profileImage.setHeight("80px");
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

	/**
	 * Creates the east panel, its contents depends on the amount of companies selected. 
	 * For 0-1 companies it will show the company and contact details. For two or more 
	 * it will show the e-mail and labels form.
	 * @return
	 */
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
	
	/**
	 * Private implementation of the ContactListener to appropriately handle when a 
	 * contact is changed.
	 * @author Rohde Fischer <rohdef@rohdef.dk>
	 */
	private class ContactEventListener implements ContactListener {
		public void handleEvent(ContactEvent be) {
			logger.log(Level.FINE, "ContactEvent happened: ");
			logger.log(Level.FINER, "\tContact: "+be.getContact());
			logger.log(Level.FINER, "\tOldContact: "+be.getOldContact() +"\n");
			
			if (be.getType() == ContactEvent.DELETED_CONTACT_TYPE) {
				logger.fine("\tContact deleted");
				companyListing.removeCompanyFromLists(be.getCompany());
				return;
			} else if (be.getType() == ContactEvent.NEW_CONTACT_TYPE) {
				logger.fine("\tNew contact recieved");
				if (be.getCompany() != null)
					companyListing.moveCompanyToCustomers(be.getCompany());
				else
					companyListing.moveContactToCustomers(be.getContact());
				
				return;
			} else if (be.getType() == ContactEvent.CHANGED_CONTACT_TYPE) {
				logger.fine("\tThe contact was changed");
				if (be.getContact().getSalesman() == null) {
					logger.fine("\t\tThe new salesman is null (therefore removed)");
					if (be.getOldContact().getSalesman() != null) {
						companyListing.moveContactToProspects(be.getContact());
					}
				} else {
					if (be.getContact().getSalesman().
							equals(be.getOldContact().getSalesman())) {
						logger.fine("\t\tThe salesman is the same");
					} else if (be.getContact().getSalesman().
							equals(Global.getInstance().getCurrentSalesman())) {
						logger.fine("\t\tThe salesman was changed");
						
						if (be.getOldContact().getSalesman() == null)
							companyListing.moveContactToCustomers(be.getContact());
					} else {
						companyListing.removeContactFromLists(be.getContact());
					}
				}
				
				return;
			} else {
				logger.log(Level.WARNING, "This shouldn't be possible");
			}
		}
	}
}