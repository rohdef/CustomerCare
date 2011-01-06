package dk.rohdef.client;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.CheckBoxListView;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Time;
import com.extjs.gxt.ui.client.widget.form.TimeField;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LatLngCallback;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import dk.rohdef.client.services.Global;
import dk.rohdef.viewmodel.Company;
import dk.rohdef.viewmodel.Contact;
import dk.rohdef.viewmodel.Salesman;

public class CreateEventDialog extends Dialog {
	private static Logger logger = Logger.getLogger(CreateEventDialog.class.getName());
	private Company company;

	private TextField<String> titleFld;
	private DateField startDateFld;
	private TextField<String> locationFld;
	private TextArea detailsFld;

	private DateField endDateFld;
	private CheckBoxListView<Salesman> salesmanContactsView;
	private CheckBoxListView<Contact> customerContactsView;
	private TimeField endTimeFld;
	private TimeField startTimeFld;
	private FormButtonBinding buttonBinding;
	private MapWidget map;
	
	public CreateEventDialog(Company company) {
		this.company = company;
		
		setModal(true);
		setHeading("Opret aftale");
		setLayout(new RowLayout(Orientation.HORIZONTAL));
		setSize(625, 450);
		
		add(createEventArea());
		add(createMapsArea());
		setMapLocation("Danmark");
		
		setButtons(Dialog.OKCANCEL);
		getButtonById(Dialog.CANCEL).setText("Anuller");
		getButtonById(Dialog.OK).setText("Opret aftale");
		
		getButtonById(Dialog.OK).addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				DateTimeFormat timeFormatter = DateTimeFormat.getFormat("yyyyMMdd");
				String startDateString = timeFormatter.format(startDateFld.getValue());
				String endDateString = timeFormatter.format(endDateFld.getValue());
				
				Time startTime = startTimeFld.getValue();
				Time endTime = endTimeFld.getValue();

				NumberFormat timeFormat = NumberFormat.getFormat("00");
				String startTimeString = timeFormat.format(startTime.getHour()) + "" 
					+ timeFormat.format(startTime.getMinutes());
				String endTimeString = timeFormat.format(endTime.getHour()) + "" 
				+ timeFormat.format(endTime.getMinutes());
				
				
				String title = titleFld.getValue();
				title = URL.encode(title);
				String dates = startDateString + "T" + startTimeString + "00Z/" +
					endDateString + "T" + endTimeString + "00Z";
				dates = URL.encode(dates);
				String location = locationFld.getValue() == null ? "" : locationFld.getValue();
				location = URL.encode(location);
				String details = detailsFld.getValue() == null ? "" : detailsFld.getValue();
				details = URL.encode(details);
				
				String guests = "";
				for (Salesman s : salesmanContactsView.getChecked()) {
					guests += s.getSalesman() + " <" + s.getMail() + ">, ";
				}
				for (Contact c : customerContactsView.getChecked()) {
					guests += c.getName() + " <" + c.getMail() +">, ";
				}
				if (guests.isEmpty() == false) {
					guests = guests.substring(0, guests.length()-2);
				}
				guests = URL.encode(guests);
				
				String sprop = "http%3A%2F%2Fwww.markon.as";
				String spropName = "name:MarkOn%20A%2FS";

				String calendarUrl = "http://www.google.com/calendar/event?action=TEMPLATE" +
						"&text=" + title +
						"&dates=" + dates +
						"&details=" + details +
						"&location=" + location +
						"&add=" + guests +
						"&trp=false" +
						"&sprop=" + sprop +
						"&sprop=" + spropName;
				
				logger.log(Level.INFO, "Accessing google calendar url: " + calendarUrl);
				
				Window.open(calendarUrl, "_blank", "");
			}
		});
		
		buttonBinding.addButton(getButtonById(Dialog.OK));
		
		setHideOnButtonClick(true);
	}

	private FormPanel createEventArea() {
		FormPanel panel = new FormPanel();
		buttonBinding = new FormButtonBinding(panel);
		panel.setHeading("Aftalens detaljer");
		
		titleFld = new TextField<String>();
		titleFld.setAllowBlank(false);
		titleFld.setAutoValidate(true);
		titleFld.setFieldLabel("Titel");
		titleFld.setValue("Møde: " + company.getCompanyName() + " og MarkOn A/S");
		panel.add(titleFld);

		FormLayout topLabelLayout = new FormLayout();
		topLabelLayout.setLabelAlign(LabelAlign.TOP);
		
		LayoutContainer left = new LayoutContainer();
		left.setLayout(topLabelLayout);
		left.setWidth("70%");
		
		startDateFld = new DateField();
		startDateFld.setAllowBlank(false);
		startDateFld.setAutoValidate(true);
		startDateFld.setAutoWidth(true);
		startDateFld.setFieldLabel("Start dato");
		left.add(startDateFld);
		
		endDateFld = new DateField();
		endDateFld.setAllowBlank(false);
		endDateFld.setAutoValidate(true);
		endDateFld.setAutoWidth(true);
		endDateFld.setFieldLabel("Slut dato");
		left.add(endDateFld);
		
		startDateFld.addListener(Events.Change, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				endDateFld.setValue(startDateFld.getValue());
			}
		});
		
		topLabelLayout = new FormLayout();
		topLabelLayout.setLabelAlign(LabelAlign.TOP);
		
		LayoutContainer right = new LayoutContainer();
		right.setLayout(topLabelLayout);
		right.setWidth("30%");
		
		startTimeFld = new TimeField();
		startTimeFld.setFieldLabel("Starttid");
		startTimeFld.setWidth(150);
		startTimeFld.setTriggerAction(TriggerAction.ALL);
		right.add(startTimeFld);
		
		endTimeFld = new TimeField();
		endTimeFld.setFieldLabel("Sluttid");
		endTimeFld.setWidth(150);
		endTimeFld.setTriggerAction(TriggerAction.ALL);
		right.add(endTimeFld);
		
		startTimeFld.addListener(Events.Change, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				Time time = startTimeFld.getValue();
				endTimeFld.setValue(time);
			}
		});
		
		LayoutContainer dateTimeContainer = new LayoutContainer();
		dateTimeContainer.setLayout(new HBoxLayout());
		dateTimeContainer.add(left);
		dateTimeContainer.add(right);
		panel.add(dateTimeContainer);
				
		locationFld = new TextField<String>();
		locationFld.setFieldLabel("Placering");
		locationFld.addListener(Events.Change, new Listener<FieldEvent>() {
			public void handleEvent(FieldEvent be) {
				setMapLocation(locationFld.getValue());
			}
		});
		panel.add(locationFld);
		
		ButtonBar locationBtnBar = new ButtonBar();		
		final Button locationMarkOnBtn = new Button("Hos MarkOn");
		
		Global.getInstance().getDataService().getAppCompany(new AsyncCallback<Company>() {
			public void onSuccess(final Company result) {
				locationMarkOnBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						String placeName = result.getCompanyName();
						String address = result.getAddress();
						String city = result.getCity();
						
						locationFld.setValue(placeName + ", " + address + ", " + city);
						setMapLocation(placeName + ", " + address + ", " + city);
					}
				});
			}
			
			public void onFailure(Throwable caught) {
			}
		});
		locationBtnBar.add(locationMarkOnBtn);
		
		Button locationAtClientBtn = new Button("Hos " + company.getCompanyName());
		locationAtClientBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				String placeName = company.getCompanyName();
				String address = company.getAddress();
				int postal = company.getPostal();
				String city = company.getCity();
				
				locationFld.setValue(placeName + ", " + address + ", " + postal + " " + city);
				setMapLocation(placeName + ", " + address + ", " + postal + " "  + city);
			}
		});
		locationBtnBar.add(locationAtClientBtn);
		panel.add(locationBtnBar);
		
		detailsFld = new TextArea();
		detailsFld.setFieldLabel("Detaljer");
		panel.add(detailsFld);

		final ListStore<Contact> custormerContactsStore = new ListStore<Contact>();
		customerContactsView = new CheckBoxListView<Contact>();
		customerContactsView.setHeight(100);
		customerContactsView.setWidth("50%");
		customerContactsView.setStore(custormerContactsStore);
		customerContactsView.setDisplayProperty("contactname");
		Global.getInstance().getDataService().getAllContacts(
//				Global.getInstance().getCurrentSalesman(),
				company,
				new AsyncCallback<ArrayList<Contact>>() {
					public void onSuccess(ArrayList<Contact> result) {
						custormerContactsStore.add(result);
					}
					
					public void onFailure(Throwable caught) {
						logger.log(Level.SEVERE, "Couldn't fetch contacts.", caught);
					}
				});
		
		final ListStore<Salesman> salesmanContactsStore = new ListStore<Salesman>();
		salesmanContactsView = new CheckBoxListView<Salesman>();
		salesmanContactsView.setWidth("50%");
		salesmanContactsView.setHeight(100);
		salesmanContactsView.setStore(salesmanContactsStore);
		salesmanContactsView.setDisplayProperty("salesman");
		Global.getInstance().getDataService().getSalesmen(new AsyncCallback<ArrayList<Salesman>>() {
			public void onSuccess(ArrayList<Salesman> result) {
				salesmanContactsStore.add(result);
			}
			public void onFailure(Throwable caught) {
				logger.log(Level.SEVERE, "Couldn't fetch salespeople", caught);
			}
		});
		
		LayoutContainer guestListContainer = new LayoutContainer();
		guestListContainer.setLayout(new HBoxLayout());
		guestListContainer.add(customerContactsView);
		guestListContainer.add(salesmanContactsView);
		panel.add(guestListContainer);
		
		return panel;
	}

	private ContentPanel createMapsArea() {
		ContentPanel panel = new ContentPanel();
		panel.setHeading("Lokation for mødet");
		panel.setSize(300, 300);
		
		map = new MapWidget();
		map.setSize("100%", "100%");
		map.setUIToDefault();
		map.setContinuousZoom(true);
		panel.add(map);
		
		return panel;
	}
	
	private void setMapLocation(final String address) {
		Geocoder geocoder = new Geocoder();
		geocoder.getLatLng(address, new LatLngCallback() {
			
			public void onSuccess(final LatLng point) {
				map.clearOverlays();
				if (address.equals("Danmark")) {
					map.setZoomLevel(5);
					map.panTo(point);
				} else {
					Marker marker = new Marker(point);
					map.addOverlay(marker);
					map.setZoomLevel(12);
					map.panTo(point);
				}
			}
			
			public void onFailure() {
				logger.finer("Address [" + address + "] not found.");
			}
		});
	}
}
