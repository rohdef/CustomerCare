package as.markon.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Set;

import com.google.gwt.dev.util.collect.HashMap;
import com.google.gwt.thirdparty.guava.common.collect.HashBiMap;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import as.markon.client.DataService;
import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;
import as.markon.viewmodel.Importance;
import as.markon.viewmodel.Salesman;
import as.markon.viewmodel.Trade;

public class DatabaseServiceImpl extends RemoteServiceServlet implements
		DataService {
	private static final long serialVersionUID = 1L;

	private Connection c;
	private ArrayList<Company> companies;
	private ArrayList<Trade> trades;

	private String url = "jdbc:postgresql://localhost/", db = "Markon",
			driver = "org.postgresql.Driver", user = "Markon",
			password = "123";
	
	private HashBiMap<Company, Integer> companyMap;
	private HashBiMap<Contact, Integer> contactMap;
	private HashBiMap<Salesman, Integer> salesmanMap;
	private HashBiMap<Trade, Integer> tradeMap;
	
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
			throw new RuntimeException(e.getMessage());
		}
	}

	public synchronized ArrayList<Company> getCompanies(int salesmanId) {
		getTrades();
		try {
			companies = new ArrayList<Company>();
			companyMap = HashBiMap.create();

			connect();
			Statement companyStatement, contactStatement;
			companyStatement = c.createStatement();
			contactStatement = c.createStatement();

			String companyQuery = "SELECT DISTINCT\n"
				+ "	c.companyid,\n"
				+ "	c.companyname,\n"
				+ "	c.address,\n"
				+ "	c.postal,\n"
				+ " p.city,\n"
				+ "	c.phone,\n"
				+ "	c.mail,\n"
				+ "	c.importance,\n"
				+ "	c.comments,\n"
				+ " c.tradeid\n"
				+ "		FROM salespeople s, companies c, contacts k, postalcodes p\n"
				+ "		WHERE c.companyid = k.companyid\n"
				+ "			AND k.salesmanid = s.salesmanid\n"
				+ "			AND s.salesmanid = " + salesmanId + ""
				+"			AND c.postal = p.postal;";

			ResultSet companyResults;
			companyResults = companyStatement.executeQuery(companyQuery);

			while (companyResults.next()) {
				Company c = new Company();

				String companyName = companyResults.getString("companyname");
				if (companyName == null)
					throw new RuntimeException("Data error occured, a company without name should not be possible.");
				c.setCompanyName(companyName);
				
				String companyAddress = companyResults.getString("address");
				if (companyAddress == null)
					companyAddress = "";
				c.setAddress(companyAddress);
				
				c.setPostal(companyResults.getInt("postal"));
				
				String companyCity = companyResults.getString("city");
				if (companyCity == null)
					companyCity = "";
				c.setCity(companyCity);
				
				String companyPhone = companyResults.getString("phone");
				if (companyPhone == null)
					companyPhone = "";
				c.setPhone(companyPhone);
				
				String companyMail = companyResults.getString("mail");
				if (companyMail == null)
					companyMail = "";
				c.setMail(companyMail);
				
				String importanceChar = companyResults.getString("importance");
				if (importanceChar == null)
					importanceChar = "I";
				c.setImportance(Importance.valueOf(importanceChar));
				
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
				
				int tradeid = companyResults.getInt("tradeid");
				if (!companyResults.wasNull())
					c.setTrade(tradeMap.inverse().get(new Integer(tradeid)));
				
				c.setContacts(contacts);
				companies.add(c);
				companyMap.put(c, companyResults.getInt("companyid"));
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

	public synchronized ArrayList<Trade> getTrades() {
		if (tradeMap == null) {
			tradeMap = HashBiMap.create();
			trades = new ArrayList<Trade>();
			
			String tradeSql = "SELECT t.tradeid, t.tradename FROM trade t";
			
			try {
				connect();
				
				Statement tradeStatement = c.createStatement();
				ResultSet tradeResult = tradeStatement.executeQuery(tradeSql);
				
				while (tradeResult.next()) {
					Trade trade = new Trade();
					trade.setTrade(tradeResult.getString("tradename"));
					
					tradeMap.put(trade, tradeResult.getInt("tradeid"));
					trades.add(trade);
				}
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
		}
		
		return trades;
	}
	
	public Importance getImportance(String name) {
		return Importance.valueOf(name);
	}

}
