package as.markon.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import as.markon.client.DataService;
import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;

public class DatabaseServiceImpl extends RemoteServiceServlet implements
		DataService {
	private static final long serialVersionUID = 1L;

	private Connection c;
	private ArrayList<Company> companies;

	private String url = "jdbc:postgresql://localhost/", db = "Markon",
			driver = "org.postgresql.Driver", user = "Markon",
			password = "123";

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

			String companyQuery = "SELECT DISTINCT" + "	c.companyid,"
					+ "	c.companyname," + "	c.address," + "	c.city,"
					+ "	c.phone," + "	c.mail," + "	c.importance,"
					+ "	c.comments\n"
					+ "		FROM salespeople s, companies c, contacts k\n"
					+ "		WHERE c.companyid = k.companyid\n"
					+ "			AND k.salesmanid = s.salesmanid\n"
					+ "			AND s.salesmanid = " + salesmanId + ";";

			ResultSet companyResults;
			companyResults = companyStatement.executeQuery(companyQuery);

			while (companyResults.next()) {
				Company c = new Company();

				c.setCompanyName(companyResults.getString("companyname"));
				c.setAddress(companyResults.getString("address"));
				c.setCity(companyResults.getString("city"));
				c.setPhone(companyResults.getString("phone"));
				c.setMail(companyResults.getString("mail"));
				// TODO Importance
				c.setComments(companyResults.getString("comments"));

				String contactQuery = "SELECT" + "	k.contactname,"
						+ "	k.title," + "	k.phone," + "	k.mail,"
						+ "	k.comments"
						+ "		FROM contacts k, companies c, salespeople s"
						+ "		WHERE s.salesmanid = " + salesmanId
						+ "			AND c.companyid = "
						+ companyResults.getInt("companyid");

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
