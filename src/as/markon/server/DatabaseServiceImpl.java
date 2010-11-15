package as.markon.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.google.gwt.dev.util.collect.HashMap;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import as.markon.client.DataService;
import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;
import as.markon.viewmodel.Salesman;
import as.markon.viewmodel.Trade;

public class DatabaseServiceImpl extends RemoteServiceServlet implements
		DataService {
	private static final long serialVersionUID = 1L;

	private Connection c;
	private ArrayList<Company> companies;

	private String url = "jdbc:postgresql://localhost/", db = "Markon",
			driver = "org.postgresql.Driver", user = "Markon",
			password = "123";
	
	private HashMap<Company, Integer> companyMap;
	private HashMap<Contact, Integer> contactMap;
	private HashMap<Salesman, Integer> salesmanMap;
	private HashMap<Trade, Integer> tradeMap;
	
	public DatabaseServiceImpl() {
		try {
			Class.forName(driver).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		connect();
	}

	private synchronized void connect() {
		try {
			if (c != null && !c.isClosed())
				return;

			c = DriverManager.getConnection(url+db, user, password);
		} catch (SQLException e) {
			// TODO add log
			throw new RuntimeException(e.getMessage());
		}
	}

	public synchronized ArrayList<Company> getCompanies(int salesmanId) {
		try {
			companies = new ArrayList<Company>();

			connect();
			Statement companyStatement, contactStatement;
			companyStatement = c.createStatement();
			contactStatement = c.createStatement();

			String companyQuery = "SELECT DISTINCT"
				+ "	c.companyid,"
				+ "	c.companyname,"
				+ "	c.address,"
				+ "	c.postal,"
				+ " p.city,"
				+ "	c.phone,"
				+ "	c.mail,"
				+ "	c.importance,"
				+ "	c.comments\n"
				+ "		FROM salespeople s, companies c, contacts k, postalcodes p\n"
				+ "		WHERE c.companyid = k.companyid\n"
				+ "			AND k.salesmanid = s.salesmanid\n"
				+ "			AND s.salesmanid = " + salesmanId + ""
				+"			AND c.postal = p.postal;";

			ResultSet companyResults;
			companyResults = companyStatement.executeQuery(companyQuery);

			while (companyResults.next()) {
				Company c = new Company();

				c.setCompanyName(companyResults.getString("companyname"));
				c.setAddress(companyResults.getString("address"));
				c.setPostal(companyResults.getInt("postal"));
				c.setCity(companyResults.getString("city"));
				c.setPhone(companyResults.getString("phone"));
				c.setMail(companyResults.getString("mail"));
				// TODO Importance
				c.setComments(companyResults.getString("comments"));

				String contactQuery = "SELECT\n" +
						"\tk.contactname,\n" +
						"\tk.title,\n" +
						"\tk.phone,\n" +
						"\tk.mail,\n" +
						"\tk.comments\n" +
						"\t\tFROM contacts k, companies c, salespeople s\n" +
						"\t\tWHERE s.salesmanid = " + salesmanId + "\n" +
						"\t\tAND c.companyid = " + companyResults.getInt("companyid") + "\n" +
						"\t\tAND k.companyid = c.companyid\n" +
						"\t\tAND k.salesmanid = s.salesmanid;";
				

				ResultSet contactResult = contactStatement
						.executeQuery(contactQuery);

				ArrayList<Contact> contacts = new ArrayList<Contact>();
				while (contactResult.next()) {
					Contact k = new Contact();
					k.setName(contactResult.getString("contactname"));
					k.setTitle(contactResult.getString("title"));
					k.setPhone(contactResult.getString("phone"));
					k.setMail(contactResult.getString("mail"));
					k.setComments(contactResult.getString("comments"));

					contacts.add(k);
				}
				
				c.setContacts(contacts);
				companies.add(c);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}

		close();
		
		return companies;
	}

	public void close() {
		try {
			c.close();
		} catch (SQLException e) {
			// TODO log
			throw new RuntimeException(e.getMessage());
		}
	}

	public ArrayList<Contact> getContactsFor(Company company) {
		return company.getContacts();
	}
}
