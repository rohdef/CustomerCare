package as.markon.server;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.mail.HtmlEmail;

//import org.apache.commons.mail.HtmlEmail;
//import org.apache.commons.mail.SimpleEmail;

import com.google.gwt.thirdparty.guava.common.collect.HashBiMap;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import as.markon.client.DataService;
import as.markon.viewmodel.City;
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
	private ArrayList<City> cities;

	private String url = "jdbc:postgresql://localhost/", db = "Markon",
			driver = "org.postgresql.Driver", user = "Markon",
			password = "123";

	private HashBiMap<Salesman, Integer> salesmanMap;
	private HashBiMap<Trade, Integer> tradeMap;
	private HashBiMap<Integer, City> cityMap;

	private ArrayList<Salesman> salespeople;

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

			c = DriverManager.getConnection(url + db, user, password);
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public synchronized ArrayList<Company> getCompanies(Salesman salesman) {
		getTrades();
		getCities();
		getSalesmen();
		int salesmanId = salesmanMap.get(salesman);

		try {
			companies = new ArrayList<Company>();

			connect();
			Statement companyStatement;
			companyStatement = c.createStatement();

			String companyQuery = "SELECT DISTINCT\n"
					+ "	c.companyid,\n"
					+ "	c.companyname,\n"
					+ "	c.address,\n"
					+ "	c.postal,\n"
					+ " c.city,\n"
					+ "	c.phone,\n"
					+ "	c.mail,\n"
					+ "	c.importance,\n"
					+ "	c.comments,\n"
					+ " c.tradeid\n"
					+ "		FROM salespeople s, companieswithcities c, contacts k\n"
					+ "		WHERE c.companyid = k.companyid\n"
					+ "			AND k.salesmanid = s.salesmanid\n"
					+ "			AND s.salesmanid = " + salesmanId + ";";

			ResultSet companyResults;
			companyResults = companyStatement.executeQuery(companyQuery);

			while (companyResults.next()) {
				Company c = new Company();

				c.set("companyid", companyResults.getInt("companyid"));

				String companyName = companyResults.getString("companyname");
				if (companyName == null)
					throw new RuntimeException(
							"Data error occured, a company without name should not be possible.");
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

				Trade noTrade = new Trade();
				noTrade.setTrade("Ingen branche valgt");
				int tradeid = companyResults.getInt("tradeid");
				if (!companyResults.wasNull())
					c.setTrade(tradeMap.inverse().get(new Integer(tradeid)));
				else
					c.setTrade(noTrade);

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

	public synchronized ArrayList<Contact> getContactsFor(Salesman salesman,
			Company company) {
		try {
			String contactQuery = "SELECT\n" + "\tk.contactname,\n"
					+ "\tk.title,\n" + "\tk.phone,\n" + "\tk.mail,\n"
					+ "\tk.comments\n" + "\t\tFROM contacts k, salespeople s\n"
					+ "\t\tWHERE s.salesmanid = " + salesmanMap.get(salesman)
					+ "\n" + "\t\tAND k.companyid = "
					+ company.get("companyid") + "\n"
					+ "\t\tAND k.salesmanid = s.salesmanid;";

			connect();
			Statement contactStatement = c.createStatement();
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

			return contacts;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
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
				tradeMap = null;
				trades = null;

				throw new RuntimeException(e.getMessage());
			}
		}

		return trades;
	}

	public Importance getImportance(String name) {
		return Importance.valueOf(name);
	}

	public synchronized ArrayList<City> getCities() {
		if (cities == null) {
			cities = new ArrayList<City>();
			cityMap = HashBiMap.create();

			try {
				connect();

				Statement cityStatement = c.createStatement();

				String citySql = "SELECT p.postal, p.city FROM postalcodes p;";
				ResultSet cityResults = cityStatement.executeQuery(citySql);

				while (cityResults.next()) {
					int postal = cityResults.getInt("postal");
					City c = new City();
					c.setPostal(postal);
					c.setCity(cityResults.getString("city"));

					cities.add(c);
					cityMap.put(postal, c);
				}
			} catch (Exception e) {
				cities = null;
				cityMap = null;

				throw new RuntimeException(e.getMessage());
			}
		}

		return cities;
	}

	public void sendMail(String user, String subject, String message,
			List<String> recipients) {
		try {
			for (String recipiant : recipients) {
				HtmlEmail mail = new HtmlEmail();
				mail.addTo(recipiant);
				mail.setFrom(user);
				mail.setSubject(subject);
				mail.setHtmlMsg(message);
				mail.setTextMsg("Dit mail-program understøtter desværre ikke html-beskeder. Du anbefales at opgradere dit mail-program.\n\n"
						+ "Your mail program does not support html-messages. We recommend that you upgrade your program.");

				mail.setHostName("pasmtp.tele.dk");
				mail.send();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public synchronized ArrayList<Salesman> getSalesmen() {
		if (salespeople == null) {
			salespeople = new ArrayList<Salesman>();
			salesmanMap = HashBiMap.create();

			String tradeSql = "SELECT s.salesmanid, s.salesman, s.mail FROM salespeople s;";

			try {
				connect();

				Statement salespeopleStatement = c.createStatement();
				ResultSet salespeopleResult = salespeopleStatement
						.executeQuery(tradeSql);

				while (salespeopleResult.next()) {
					Salesman salesman = new Salesman();
					salesman.setSalesman(salespeopleResult
							.getString("salesman"));
					salesman.setMail(salespeopleResult.getString("mail"));

					salespeople.add(salesman);
					salesmanMap.put(salesman,
							salespeopleResult.getInt("salesmanid"));
				}
			} catch (Exception e) {
				salespeople = null;
				salesmanMap = null;

				throw new RuntimeException(e.getMessage());
			}
		}

		return salespeople;
	}

	public void sendTrade(Trade t) {
	}

	public void sendImportance(Importance i) {
	}

	public Integer createCompany(Company company) {
		connect();

		try {
			String storedCall = "{? = call insertCompany " +
					"(?, ?, ?, ?, ?, ?, ?, ?) }";
			CallableStatement insertProc = c.prepareCall(storedCall);
			insertProc.registerOutParameter(1, Types.INTEGER);
			
			insertProc.setString(2, company.getCompanyName());
			insertProc.setString(3, company.getAddress());
			insertProc.setInt(4, company.getPostal());
			insertProc.setString(5, company.getPhone());
			insertProc.setString(6, company.getMail());
			insertProc.setNull(7, Types.INTEGER); // Trade
			insertProc.setString(8, company.getImportance().name());
			insertProc.setString(9, company.getComments());
			
			insertProc.execute();
			int companyid = insertProc.getInt(1); 
			
			return companyid;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
