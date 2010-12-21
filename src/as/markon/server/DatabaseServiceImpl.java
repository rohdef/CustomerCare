package as.markon.server;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.mail.HtmlEmail;
import org.apache.log4j.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import as.markon.client.services.DataService;
import as.markon.viewmodel.City;
import as.markon.viewmodel.Company;
import as.markon.viewmodel.Contact;
import as.markon.viewmodel.Importance;
import as.markon.viewmodel.LabelRecipient;
import as.markon.viewmodel.Salesman;
import as.markon.viewmodel.Trade;

public class DatabaseServiceImpl extends RemoteServiceServlet implements
		DataService {
	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger(DatabaseServiceImpl.class);

	private ArrayList<Trade> trades;
	private ArrayList<City> cities;
	private ArrayList<Salesman> salespeople;

	private Connection c;
	private String url, database, driver, user, password;

	private HashMap<Integer, Trade> tradeMap;
	private HashMap<Integer, City> cityMap;
	private XMLConfiguration config;

	public DatabaseServiceImpl() {
		try {
			config = new XMLConfiguration("config.xml");
			
			driver = "org.postgresql.Driver";
			url = "jdbc:postgresql://" + config.getString("database.host") + ":" +
				config.getInt("database.port", 5432) + "/";
			user = config.getString("database.user");
			password = config.getString("database.password");
			database = config.getString("database.database");
		} catch (ConfigurationException ex) {
			logger.fatal("Configuration failed", ex);
		}
		
		
		try {
			Class.forName(driver).newInstance();
		} catch (InstantiationException e) {
			logger.fatal("Can not instantiate database driver", e);
		} catch (IllegalAccessException e) {
			logger.fatal("Permission problem for the database driver", e);
		} catch (ClassNotFoundException e) {
			logger.fatal("Could not find the database driver", e);
		}

		connect();
	}

	private synchronized void connect() {
		try {
			if (c != null && !c.isClosed())
				return;

			logger.info("Connecting to database");
			c = DriverManager.getConnection(url + database, user, password);
			logger.info("Connected");
		} catch (SQLException e) {
			logger.fatal("Database connection failed", e);
			throw new RuntimeException("Kunne ikke oprette forbindelse til databasen.");
		}
	}

	public void close() {
		try {
			logger.info("Closing database");
			c.close();
			logger.info("Connection closed");
		} catch (SQLException e) {
			logger.error("Could not close database connection", e);
		}
	}

	public Importance getImportance(String name) {
		return Importance.valueOf(name);
	}

	public synchronized ArrayList<City> getCities() {
		if (cities == null) {
			logger.info("Preparing to fetch cities");
			
			cities = new ArrayList<City>();
			cityMap = new HashMap<Integer, City>();

			try {
				connect();

				Statement cityStatement = c.createStatement();

				String citySql = "SELECT p.postal, p.city FROM postalcodes p;";
				logger.info("Fetching cities");
				logger.debug("\tThe sql being used is: " + citySql);
				ResultSet cityResults = cityStatement.executeQuery(citySql);

				while (cityResults.next()) {
//					logger.debug("\tCity fetched:");
					int postal = cityResults.getInt("postal");
//					logger.debug("\t\tPostal: "+postal);
					String cityname = cityResults.getString("city");
//					logger.debug("\t\tCity name: "+cityname);
					City c = new City();
					c.setPostal(postal);
					c.setCity(cityname);

					cities.add(c);
					cityMap.put(postal, c);
				}
				logger.info("\t"+cities.size() + " cities fetched");
			} catch (Exception e) {
				cities = null;
				cityMap = null;

				logger.fatal("Get cities", e);
				throw new RuntimeException("Kunne ikke hente listen af byer");
			}
		}

		return cities;
	}

	public void sendMail(String user, String subject, String message,
			List<String> recipients) {
		try {
			logger.info("Starting to send mails");
			
			for (String recipiant : recipients) {
				HtmlEmail mail = new HtmlEmail();
				mail.addTo(recipiant);
				mail.setFrom(user);
				mail.setSubject(subject);
				mail.setHtmlMsg(message);
				mail.setTextMsg("Dit mail-program understøtter desværre ikke " +
						"html-beskeder. Du anbefales at opgradere dit mail-program.\n\n"
						+ "Your mail program does not support html-messages. We " +
								"recommend that you upgrade your program.");

				mail.setHostName("pasmtp.tele.dk");
				logger.debug("\tSending the mail\n\t\tSubject: "+subject+"\n\t\tMessage: "+mail
						+"\n\n\t\tTo: "+recipiant+"\n\t\tFrom: "+user+"\n");
				
				mail.send();
			}
		} catch (Exception e) {
			logger.fatal("Send mails", e);
			throw new RuntimeException("Kunne ikke sende mail.");
		}
	}

	public void sendImportance(Importance i) {
	}

	//
	// Companies
	//
	public synchronized ArrayList<Company> getCompanies(Salesman salesman) {
		logger.info("Fetching companies");
		getTrades();
		getCities();
		getSalesmen();
		int salesmanId = salesman.get("salesmanid");
		logger.debug("\tThe salesman id is: "+salesmanId);

		try {
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
					+ "	c.importance,\n"
					+ "	c.comments,\n"
					+ " c.tradeid\n"
					+ "		FROM salespeople s, companieswithcities c, contacts k\n"
					+ "		WHERE c.companyid = k.companyid\n"
					+ "			AND k.salesmanid = s.salesmanid\n"
					+ "			AND s.salesmanid = " + salesmanId + ";";
			logger.debug("\tThe sql being used is: "+companyQuery);			

			ResultSet companyResults;
			companyResults = companyStatement.executeQuery(companyQuery);

			ArrayList<Company> companies = fillCompanyArrayList(companyResults);
			logger.info(companies.size() + " companies fetched");
			return companies;
		} catch (SQLException e) {
			logger.fatal("Fetch customers for "+salesman.get("salesmanid"), e);
			throw new RuntimeException("Kunne ikke hente kundelisten for "
					+ salesman.getSalesman());
		}
		
	}

	public Company getAppCompany() {
		Company appCompany = new Company();
		appCompany.setCompanyName(config.getString("company.name"));
		appCompany.setAddress(config.getString("company.address"));
		appCompany.setCity(config.getString("company.city"));
		appCompany.set("country", config.getString("company.country"));
		appCompany.setPhone(config.getString("company.phone"));
		appCompany.set("fax", config.getString("company.fax"));
		appCompany.set("webpage", config.getString("company.webpage"));
		appCompany.set("vat-no", config.getString("company.vat-no"));
		
		return appCompany;
	}
	
	private ArrayList<Company> fillCompanyArrayList(ResultSet companyResults)
			throws SQLException {
		ArrayList<Company> companies = new ArrayList<Company>();
		while (companyResults.next()) {
			int companyid = companyResults.getInt("companyid");
			String companyName = companyResults.getString("companyname");
			String companyAddress = companyResults.getString("address");
			String companyCity = companyResults.getString("city");
			int postal = companyResults.getInt("postal");
			String companyPhone = companyResults.getString("phone");
			String importanceChar = companyResults.getString("importance");
			String comments = companyResults.getString("comments");
			int tradeid = companyResults.getInt("tradeid");
			
			logger.debug("\tCompany fetched:");
			logger.debug("\t\tID: " + companyid);
			logger.debug("\t\tName: " + companyName);
			logger.debug("\t\tAddress: " + companyAddress);
			logger.debug("\t\tCity (should be blank or null in most cases):" + companyCity);
			logger.debug("\t\tPostal: " + postal);
			logger.debug("\t\tPhone: " + companyPhone);
			logger.debug("\t\tImportance: " + importanceChar);
			logger.debug("\t\tComments: " + comments);
			logger.debug("\t\tTrade id: " + tradeid);

			Company c = new Company();
			c.set("companyid", companyid);

			if (companyName == null)
				throw new RuntimeException(
						"Data error occured, a company without name should not be possible.");
			c.setCompanyName(companyName);

			if (companyAddress == null)
				companyAddress = "";
			c.setAddress(companyAddress);

			c.setPostal(postal);

			if (companyCity == null)
				companyCity = "";
			c.setCity(companyCity);

			if (companyPhone == null)
				companyPhone = "";
			c.setPhone(companyPhone);

			if (importanceChar == null)
				importanceChar = "I";
			c.setImportance(Importance.valueOf(importanceChar));

			c.setComments(comments);

			Trade noTrade = new Trade();
			noTrade.setTrade("Ingen branche valgt");
			if (!companyResults.wasNull())
				c.setTrade(tradeMap.get(new Integer(tradeid)));
			else
				c.setTrade(noTrade);
			
			companies.add(c);
			logger.debug("\t\tSuccessfully added\n");
		}
		return companies;
	}
	
	public synchronized ArrayList<Company> getProspectCompanies() {
		logger.info("Getting prospect companies");
		getTrades();
		getCities();
		getSalesmen();

		try {
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
					+ "	c.importance,\n"
					+ "	c.comments,\n"
					+ " c.tradeid\n"
					+ "		FROM companieswithcities c\n"
					+ "		WHERE c.companyid NOT IN\n"
					+ "			(SELECT k.companyid FROM contacts k\n"
					+ "				WHERE k.salesmanid IS NOT null);";
			logger.debug("\tSql used is: " + companyQuery);

			ResultSet companyResults;
			companyResults = companyStatement.executeQuery(companyQuery);

			ArrayList<Company> companies = fillCompanyArrayList(companyResults);
			logger.info(companies.size() + " prospects fetched");
			return companies;
		} catch (SQLException e) {
			logger.fatal("Get prospects", e);
			throw new RuntimeException("Kunne ikke hente listen af potentielle kunder.");
		}
	}
	
	public synchronized Integer createCompany(Company company,
			ArrayList<Contact> contacts, Salesman salesman) {
		connect();

		try {
			logger.info("Inserting company");
			String storedCall = "{? = call insertCompany " +
					"(?, ?, ?, ?, ?, ?, ?) }";
			CallableStatement insertProc = c.prepareCall(storedCall);
			insertProc.registerOutParameter(1, Types.INTEGER);
			
			insertProc.setString(2, company.getCompanyName());
			insertProc.setString(3, company.getAddress());
			if (company.get("postal") != null)
				insertProc.setInt(4, company.getPostal());
			else
				insertProc.setNull(4, Types.INTEGER);
			insertProc.setString(5, company.getPhone());
			
			if (company.getTrade() == null || company.getTrade().get("tradeid") == null)
				insertProc.setNull(6, Types.INTEGER);
			else
				insertProc.setInt(6, company.getTrade().getId());
						
			insertProc.setString(7, company.getImportance().name());
			insertProc.setString(8, company.getComments());
			
			insertProc.execute();
			logger.info("\tSuccessfully inserted the company");
			
			int companyid = insertProc.getInt(1);
			int salesmanid = salesman.get("salesmanid");
			
			logger.info("\tInserting contacts");
			for (Contact c : contacts)
				insertContact(c, salesmanid, companyid);
			
			return companyid;
		} catch (Exception e) {
			logger.fatal("Trying to insert company", e);
			throw new RuntimeException("Kunne ikke oprette kunde.");
		}
	}

	public void updateCompany(Company company) {
		connect();

		try {
			logger.info("Updating company");
			String storedCall = "{call updateCompany " +
					"(?, ?, ?, ?, ?, ?, ?, ?) }";
			
			Trade trade = company.getTrade();
			Integer tradeid = null;
			if (trade != null)
				tradeid = trade.get("tradeid");
			
			CallableStatement insertProc = c.prepareCall(storedCall);
			insertProc.setInt(1, (Integer) company.get("companyid"));
			insertProc.setString(2, company.getCompanyName());
			insertProc.setString(3, company.getAddress());
			if (company.getPostal() == 0)
				insertProc.setNull(4, Types.INTEGER);
			else
				insertProc.setInt(4, company.getPostal());
			insertProc.setString(5, company.getPhone());
			if (tradeid == null)
				insertProc.setNull(6, Types.INTEGER);
			else
				insertProc.setInt(6, tradeid);
			insertProc.setString(7, company.getImportance().name());
			insertProc.setString(8, company.getComments());
			
			insertProc.execute();
			logger.info("\tSuccessfully updated");
		} catch (Exception e) {
			logger.fatal("Update company: "+company.get("companyid"), e);
			throw new RuntimeException("Kunne ikke opdatere: "+company.getCompanyName());
		}
	}

	public synchronized void deleteCompanies(List<Company> companies) {
		logger.info("Delete companies");
		for (Company c : companies)
			deleteCompany(c);
	}

	public void deleteCompany(Company company) {
		connect();

		try {
			logger.info("Deleting company");
			String storedCall = "{call deleteCompany ( ? ) }";
			
			CallableStatement insertProc = c.prepareCall(storedCall);
			insertProc.setInt(1, (Integer) company.get("companyid"));
			
			insertProc.execute();
			logger.info("\tSuccessfully deleted company");
		} catch (Exception e) {
			logger.fatal("Delete company "+company.get("companyid"), e);
			throw new RuntimeException("Kunne ikke slette: "+company.getCompanyName());
		}
	}
	
	public Company getCompanyFor(Contact contact) {
		Company company = null;
		
		try {
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
					+ "	c.importance,\n"
					+ "	c.comments,\n"
					+ " c.tradeid\n"
					+ "		FROM companieswithcities c, contacts k\n"
					+ "		WHERE c.companyid = k.companyid\n"
					+ "			AND k.contactid = " + contact.get("contactid") + ";";
			logger.debug("\tThe sql being used is: "+companyQuery);			

			ResultSet companyResults;
			companyResults = companyStatement.executeQuery(companyQuery);

			ArrayList<Company> companies = fillCompanyArrayList(companyResults);
			if (companies.size() == 1) {
				company = companies.get(0);
			} else if (companies.size() > 1) {
				logger.fatal("Error, wrong amount of companies returned. "
						+ companies.size() + " found, but 1 was expected.");
				throw new RuntimeException("Forkert antal firmaer fundet for kontakten "
						+ contact.getName());
			}
		} catch (SQLException e) {
			logger.fatal("Fetch company for "+contact.get("contactid"), e);
			throw new RuntimeException("Kunne ikke hente firmaet for " +
					contact.getName());
		}
		
		return company;
	}
	
	//
	// Contacts
	//
	public void insertContact(Contact contact, int salesmanid, int companyid) {
		connect();
		
		try {
			String storedCall = "{? = call insertContact " +
			"(?, ?, ?, ?, ?, ?, ?, ?) }";
			CallableStatement insertProc = c.prepareCall(storedCall);
			insertProc.registerOutParameter(1, Types.INTEGER);
			
			insertProc.setInt(2, companyid);
			insertProc.setInt(3, salesmanid);
			
			String contactName = "";
			String title = "";
			String phone = "";
			String mail = "";
			Boolean acceptsmails = false;
			String comments = "";
			
			if (contact.getName() != null)
				contactName = contact.getName();
			if (contact.getTitle() != null)
				title = contact.getTitle();
			if (contact.getPhone() != null)
				phone = contact.getPhone();
			if (contact.getMail() != null)
				mail = contact.getMail();
			if (contact.getAcceptsMails() != null)
				acceptsmails = contact.getAcceptsMails();
			if (contact.getComments() != null)
				comments = contact.getComments();
			
			insertProc.setString(4, contactName);
			insertProc.setString(5, title);
			insertProc.setString(6, phone);
			insertProc.setString(7, mail);
			insertProc.setBoolean(8, acceptsmails);
			insertProc.setString(9, comments);
			
			insertProc.execute();
		} catch (Exception e) {
			logger.fatal("Insert contact", e);
			throw new RuntimeException("Kunne ikke oprette kontaktpersonen: "+contact.getName());
		}
	}
	
	public void updateContact(Contact contact) {
		connect();

		try {
			String storedCall = "{call updateContact " +
					"(?, ?, ?, ?, ?, ?, ?, ?) }";
			
			CallableStatement insertProc = c.prepareCall(storedCall);
			insertProc.setInt(1, (Integer) contact.get("contactid"));
			insertProc.setInt(2, (Integer) contact.getSalesman().get("salesmanid"));
			insertProc.setString(3, contact.getName());
			insertProc.setString(4, contact.getTitle());
			insertProc.setString(5, contact.getPhone());
			insertProc.setString(6, contact.getMail());
			insertProc.setBoolean(7, contact.getAcceptsMails());
			insertProc.setString(8, contact.getComments());
			
			insertProc.execute();
		} catch (Exception e) {
			logger.fatal("Update contact "+contact.get("contactid"), e);
			throw new RuntimeException("Kunne ikke opdatere kontaktpersonen: "+contact.getName());
		}
	}
	
	public synchronized void deleteContacts(List<Contact> contacts) {
		for (Contact c : contacts)
			deleteContact(c);
	}
	
	public void deleteContact(Contact contact) {
		connect();

		try {
			String storedCall = "{call deleteContact ( ? ) }";
			
			CallableStatement insertProc = c.prepareCall(storedCall);
			insertProc.setInt(1, (Integer) contact.get("contactid"));
			
			insertProc.execute();
		} catch (Exception e) {
			logger.fatal("Delete contact "+contact.get("contactid"), e);
			throw new RuntimeException("Kunne ikke slette kontakten: "+contact.getName());
		}
	}

	public synchronized ArrayList<Contact> getContactsFor(Salesman salesman,
			Company company) {
		try {
			String contactQuery = "SELECT\n" +
				"\tk.contactid,\n" +
				"\tk.contactname,\n" +
				"\tk.title,\n" +
				"\tk.phone,\n" +
				"\tk.mail,\n" +
				"\tk.acceptsmails,\n" +
				"\tk.comments\n" +
				"\t\tFROM contacts k\n" +
				"\t\tWHERE k.companyid = " + company.get("companyid") + "\n";
			
			if (salesman != null)
				contactQuery += "\t\tAND k.salesmanid = " + salesman.get("salesmanid") + ";";
			else
				contactQuery += "\t\tAND k.salesmanid IS NULL;";

			connect();
			Statement contactStatement = c.createStatement();
			ResultSet contactResult = contactStatement
					.executeQuery(contactQuery);

			ArrayList<Contact> contacts = new ArrayList<Contact>();
			while (contactResult.next()) {
				Contact k = new Contact();
				k.set("contactid", contactResult.getInt("contactid"));
				k.setName(contactResult.getString("contactname"));
				k.setTitle(contactResult.getString("title"));
				k.setPhone(contactResult.getString("phone"));
				k.setMail(contactResult.getString("mail"));
				k.setComments(contactResult.getString("comments"));
				k.setAcceptsMails(contactResult.getBoolean("acceptsmails"));
				k.setSalesman(salesman);

				contacts.add(k);
			}

			return contacts;
		} catch (Exception e) {
			logger.fatal("Get contacts for company "+company.get("companyid")+
					" and salesman "+salesman.get("salesmanid"), e);
			throw new RuntimeException("Kunne ikke hente kontaktpersonerne til: " +
					company.getCompanyName());
		}
	}

	public ArrayList<Contact> getAllContacts(Company company) {
		try {
			String contactQuery = "SELECT\n" +
				"\tk.contactid,\n" +
				"\tk.contactname,\n" +
				"\tk.title,\n" +
				"\tk.phone,\n" +
				"\tk.mail,\n" +
				"\tk.acceptsmails,\n" +
				"\tk.comments\n" +
				"\t\tFROM contacts k\n" +
				"\t\tWHERE k.companyid = " + company.get("companyid") + ";";

			connect();
			Statement contactStatement = c.createStatement();
			ResultSet contactResult = contactStatement
					.executeQuery(contactQuery);

			ArrayList<Contact> contacts = new ArrayList<Contact>();
			while (contactResult.next()) {
				Contact k = new Contact();
				k.set("contactid", contactResult.getInt("contactid"));
				k.setName(contactResult.getString("contactname"));
				k.setTitle(contactResult.getString("title"));
				k.setPhone(contactResult.getString("phone"));
				k.setMail(contactResult.getString("mail"));
				k.setComments(contactResult.getString("comments"));
				k.setAcceptsMails(contactResult.getBoolean("acceptsmails"));

				contacts.add(k);
			}

			return contacts;
		} catch (Exception e) {
			logger.fatal("Get contacts for company "+company.get("companyid"), e);
			throw new RuntimeException("Kunne ikke hente kontaktpersonerne til: " +
					company.getCompanyName());
		}
	}
	
	//
	// Salespeople
	//
	public synchronized ArrayList<Salesman> getSalesmen() {
		if (salespeople == null) {
			salespeople = new ArrayList<Salesman>();

			String tradeSql = "SELECT s.salesmanid, s.salesman, s.title, s.phone, s.mail\n" +
					"FROM salespeople s ORDER BY s.salesmanid;";

			try {
				connect();

				Statement salespeopleStatement = c.createStatement();
				ResultSet salespeopleResult = salespeopleStatement
						.executeQuery(tradeSql);

				while (salespeopleResult.next()) {
					Salesman salesman = new Salesman();
					salesman.setSalesman(salespeopleResult.getString("salesman"));
					salesman.setTitle(salespeopleResult.getString("title"));
					salesman.setPhone(salespeopleResult.getString("phone"));
					salesman.setMail(salespeopleResult.getString("mail"));
					salesman.set("mailmd5", MD5Util.md5Hex(salesman.getMail()));

					salespeople.add(salesman);
					salesman.set("salesmanid", salespeopleResult.getInt("salesmanid"));
				}
			} catch (Exception e) {
				salespeople = null;

				logger.fatal("Get salespeople", e);
				throw new RuntimeException("Kunne ikke hente listen af sælgere.");
			}
		}

		return salespeople;
	}
	
	public int insertSalesman(Salesman salesman) {
		connect();
		
		try {
			String storedCall = "{? = call insertSalesman " +
			"(?, ?, ?, ?) }";
			CallableStatement insertProc = c.prepareCall(storedCall);
			insertProc.registerOutParameter(1, Types.INTEGER);
			
			String phone = "";
			
			String name = salesman.getSalesman();
			String title = salesman.getTitle();
			if (salesman.getPhone() != null)
				phone = salesman.getPhone();
			String mail = salesman.getMail();
			
			insertProc.setString(2, name);
			insertProc.setString(3, title);
			insertProc.setString(4, phone);
			insertProc.setString(5, mail);
			
			insertProc.execute();
			
			int salesmanid = insertProc.getInt(1);
			salesman.set("salesmanid", salesmanid);
			
			salespeople.add(salesman);
			
			return salesmanid;
		} catch (Exception e) {
			logger.fatal("Insert s", e);
			throw new RuntimeException("Kunne ikke oprette sælgeren: "+salesman.getSalesman());
		}
	}
	
	public void updateSalesman(Salesman salesman) {
		connect();

		try {
			String storedCall = "{call updateSalesman " +
					"(?, ?, ?, ?, ?) }";
			
			CallableStatement insertProc = c.prepareCall(storedCall);
			insertProc.setInt(1, (Integer) salesman.get("salesmanid"));
			insertProc.setString(2, salesman.getSalesman());
			insertProc.setString(3, salesman.getTitle());
			insertProc.setString(4, salesman.getPhone());
			insertProc.setString(5, salesman.getMail());
			
			insertProc.execute();
			
			for (Salesman s : salespeople) {
				if (s.get("salesmanid").equals(salesman.get("salesmanid"))) {
					s.setProperties(salesman.getProperties());
					break;
				}
			}
				
		} catch (Exception e) {
			logger.fatal("Update salesman "+salesman.get("salesmanid"), e);
			throw new RuntimeException("Kunne ikke opdatere sælgeren: "+salesman.getSalesman());
		}
	}
	
	public void deleteSalesman(Salesman salesman) {
		connect();

		try {
			String storedCall = "{call deleteSalesman ( ? ) }";
			
			CallableStatement insertProc = c.prepareCall(storedCall);
			insertProc.setInt(1, (Integer) salesman.get("salesmanid"));
			
			insertProc.execute();
			salespeople.remove(salesman);
		} catch (Exception e) {
			logger.fatal("Delete salesman "+salesman.get("contactid"), e);
			throw new RuntimeException("Kunne ikke slette sælgeren: "+salesman.getSalesman());
		}
	}
	
	//
	// Trades
	//
	public synchronized ArrayList<Trade> getTrades() {
		tradeMap = new HashMap<Integer, Trade>();
		trades = new ArrayList<Trade>();

		String tradeSql = "SELECT t.tradeid, t.tradename FROM trade t";

		try {
			connect();

			Statement tradeStatement = c.createStatement();
			ResultSet tradeResult = tradeStatement.executeQuery(tradeSql);

			logger.info("Getting trades");
			while (tradeResult.next()) {
				Trade trade = new Trade();
				trade.setTrade(tradeResult.getString("tradename"));
				trade.set("tradeid", tradeResult.getInt("tradeid"));

				tradeMap.put(tradeResult.getInt("tradeid"), trade);
				trades.add(trade);
			}
			logger.info(trades.size() + " trades fetched");
		} catch (Exception e) {
			tradeMap = null;
			trades = null;

			logger.fatal("Could not get trades", e);
			throw new RuntimeException("Kunne ikke hente listen af brancer");
		}
		return trades;
	}

	public void addTrade(Trade trade) {
		connect();
		
		try {
			String storedCall = "{call insertTrade (?, ?) }";
			CallableStatement insertProc = c.prepareCall(storedCall);
			
			insertProc.setInt(1, trade.getId());
			insertProc.setString(2, trade.getTrade());
			
			insertProc.execute();
		} catch (Exception e) {
			logger.fatal("Insert trade", e);
			throw new RuntimeException("Kunne ikke oprette branchen: "+trade.getTrade());
		}
	}
	
	public void deleteTrade(Trade trade) {
		connect();
		
		try {
			String storedCall = "{call deleteTrade (?) }";
			CallableStatement insertProc = c.prepareCall(storedCall);
			
			insertProc.setInt(1, trade.getId());
			
			insertProc.execute();
		} catch (Exception e) {
			logger.fatal("Delete trade", e);
			throw new RuntimeException("Kunne ikke slette branchen: "+trade.getTrade());
		}
	}
	
	private static class MD5Util {
		  public static String hex(byte[] array) {
		      StringBuffer sb = new StringBuffer();
		      for (int i = 0; i < array.length; ++i) {
			  sb.append(Integer.toHexString((array[i]
			      & 0xFF) | 0x100).substring(1,3));        
		      }
		      return sb.toString();
		  }
		  
		  public static String md5Hex (String message) {
		      try {
			  MessageDigest md = 
			      MessageDigest.getInstance("MD5");
			  return hex (md.digest(message.getBytes("CP1252")));
		      } catch (NoSuchAlgorithmException e) {
		      } catch (UnsupportedEncodingException e) {
		      }
		      return null;
		  }
		}


	public Integer createPdf(ArrayList<LabelRecipient> recipients) {
		connect();
		Integer batchid = 0;
		
		try {
			
			String batchIdCall = "{ ? = call nextval('labelqueue_batchid_seq') }";
			CallableStatement batchIdProc = c.prepareCall(batchIdCall);
			
			batchIdProc.registerOutParameter(1, Types.BIGINT);
			batchIdProc.execute();
			batchid = (int) batchIdProc.getLong(1);
			
			String storedCall = "{ call labelQueueAddCompany (?, ?) }";
			CallableStatement companyProc = c.prepareCall(storedCall);			
			companyProc.setInt(1, batchid);
			
			storedCall = "{ call labelQueueAddContact (?, ?) }";
			CallableStatement contactProc = c.prepareCall(storedCall);
			contactProc.setInt(1, batchid);
			
			for (LabelRecipient recipient : recipients) {
				Company company = recipient.getCompany();
				Contact contact = recipient.getContact();
				
				if (company != null && contact != null) {
					String error = "Invalid LabelRecipeint recieved"
						+ " both contact nor company was set";
					logger.fatal(error);
					throw new IllegalArgumentException(error);
				} else if (company != null) {
					companyProc.setInt(2, (Integer) company.get("companyid"));
					companyProc.execute();
				} else if (contact != null) {
					contactProc.setInt(2, (Integer) contact.get("contactid"));
					contactProc.execute();
				} else {
					String error = "Invalid LabelRecipeint recieved"
						+ " neither contact nor company was set";
					logger.fatal(error);
					throw new NullPointerException(error);
				}
			}
			
			c.close();
		} catch (Exception e) {
			logger.fatal("Trying to queue company for mail", e);
			throw new RuntimeException("Kunne ikke oprette post-data.");
		}
		
		return batchid;
	}
}