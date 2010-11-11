package as.markon.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;

public class DatabaseService {
	private Connection c;
	private List<Company> companies;
	
	public DatabaseService() {
		connect();
	}

	private synchronized void connect() {
		try {
			if (c != null && !c.isClosed())
				return;
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		try {
			c = DriverManager.getConnection("jdbc:postgresql://localhost/Markon", "Markon", "123");
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public synchronized List<Company> getCompanies(int salesmanId) throws SQLException {
		companies = new ArrayList<Company>();
		
		connect();
		Statement companyStatement, contactStatement;
		companyStatement = c.createStatement();
		contactStatement = c.createStatement();
		
		String companyQuery = "SELECT DISTINCT" +
				"	c.companyid," +
				"	c.companyname," +
				"	c.address," +
				"	c.city," +
				"	c.phone," +
				"	c.mail," +
				"	c.importance," +
				"	c.comments\n"+
				"		FROM salespeople s, companies c, contacts k\n"+
				"		WHERE c.companyid = k.companyid\n"+
				"			AND k.salesmanid = s.salesmanid\n"+
				"			AND s.salesmanid = "+salesmanId+";";
		
		ResultSet companyResults;
		companyResults = companyStatement.executeQuery(companyQuery);

		try {
			while (companyResults.next()) {
				Company c = new Company();
				
				c.setCompanyName(companyResults.getString("companyname"));
				c.setAddress(companyResults.getString("address"));
				c.setCity(companyResults.getString("city"));
				c.setPhone(companyResults.getString("phone"));
				c.setMail(companyResults.getString("mail"));
				// TODO Importance			
				c.setComments(companyResults.getString("comments"));
				
				
				String contactQuery = "SELECT" +
						"	k.contactname," +
						"	k.title," +
						"	k.phone," +
						"	k.mail," +
						"	k.comments" +
						"		FROM contacts k, companies c, salespeople s" +
						"		WHERE s.salesmanid = "+salesmanId+
						"			AND c.companyid = "+companyResults.getInt("companyid");
				
				ResultSet contactResult = contactStatement.executeQuery(contactQuery);
				
				List<Contact> contacts = new ArrayList<Contact>();
				while (contactResult.next()) {
					Contact k = new Contact();
					k.setName(contactResult.getString("contactname"));
					k.setTitle(contactResult.getString("title"));
					k.setPhone(contactResult.getString("phone"));
					k.setMail(contactResult.getString("mail"));
					k.setComments(contactResult.getString("comments"));
				}
				
				c.setContacts(contacts);
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return companies;
	}
	
	public void close() {
		try {
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
