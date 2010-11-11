package as.markon.client;

import java.util.ArrayList;
import java.util.List;

import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;
import as.markon.viewmodel.Importance;

public class DemoDataService {
	public static List<Company> getCompanies() {
		List<Company> companies = new ArrayList<Company>();
		
		Company cmp;
		Contact ctc;
		
		cmp = new Company();
		cmp.setCompanyName("Falsk Markon");
		cmp.setAddress("Bullervej 15");
		cmp.setCity("8000");
		cmp.setPhone("44332211");
		cmp.setMail("c1@storvirksomhed.dk");
		cmp.setImportance(Importance.Silver);
		cmp.setComments("Einar er ubehagelig, spoerg efter Hans. Ud over det meget flinke.");
		
		List<Contact> contacts = new ArrayList<Contact>();
		
		ctc = new Contact();
		ctc.setName("John");
		ctc.setTitle("Sjaef");
		ctc.setPhone("12");
		ctc.setMail("john@markon.as");
		contacts.add(ctc);

		ctc = new Contact();
		ctc.setName("Jesper");
		ctc.setTitle("Anden sjaef");
		ctc.setPhone("16");
		ctc.setMail("jesper@markon.as");
		contacts.add(ctc);
		
		ctc = new Contact();
		ctc.setName("Lars");
		ctc.setTitle("Tredie sjaef");
		ctc.setPhone("25");
		ctc.setMail("lars@markon.as");
		contacts.add(ctc);
		
		ctc = new Contact();
		ctc.setName("Susanne");
		ctc.setTitle("Salg");
		ctc.setPhone("15");
		ctc.setMail("susanne@markon.as");
		contacts.add(ctc);
		
		cmp.setContacts(contacts);
		
		companies.add(cmp);
		
		cmp = new Company();
		cmp.setCompanyName("Company 2");
		cmp.setAddress("Fiktiv Alle 15");
		cmp.setCity("8200");
		cmp.setPhone("88776655");
		cmp.setMail("penge@megetstorvirksomhed.dk");
		cmp.setImportance(Importance.Gold);
		cmp.setComments("De bruger mange penge og er super soede, siger man ged til dem giver de en julefrokost.");
		
		contacts = new ArrayList<Contact>();
		
		cmp.setContacts(contacts);
		companies.add(cmp);
		
		cmp = new Company();
		cmp.setCompanyName("Dev Inc");
		cmp.setAddress("Bentesvej 666");
		cmp.setCity("8200");
		cmp.setPhone("90066600");
		cmp.setMail("lucifer@devinc.usa");
		cmp.setImportance(Importance.Bronce);
		cmp.setComments("De er rigtigt traels, men penge er penge");
		
		contacts = new ArrayList<Contact>();
		
		cmp.setContacts(contacts);
		companies.add(cmp);
		
		cmp = new Company();
		cmp.setCompanyName("Virksomehder");
		cmp.setAddress("Storegade 1");
		cmp.setCity("8000");
		cmp.setPhone("66556655");
		cmp.setMail("virk@virk.dk");
		cmp.setImportance(Importance.Silver);
		cmp.setComments("Ingen kommentar");
		
		contacts = new ArrayList<Contact>();
		
		cmp.setContacts(contacts);
		companies.add(cmp);
		
		return companies;
	}
}
