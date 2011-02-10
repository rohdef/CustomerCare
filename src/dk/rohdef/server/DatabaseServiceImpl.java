package dk.rohdef.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.mail.HtmlEmail;
import org.apache.log4j.Logger;
import org.scb.gwt.web.server.i18n.GWTI18N;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import dk.rohdef.client.i18n.CustomerCareI18n;
import dk.rohdef.client.services.DataService;
import dk.rohdef.viewmodel.City;
import dk.rohdef.viewmodel.Company;
import dk.rohdef.viewmodel.Contact;
import dk.rohdef.viewmodel.Importance;
import dk.rohdef.viewmodel.LabelRecipient;
import dk.rohdef.viewmodel.Salesman;
import dk.rohdef.viewmodel.Trade;


public class DatabaseServiceImpl extends RemoteServiceServlet implements
		DataService {
	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger(DatabaseServiceImpl.class);

	private ArrayList<Trade> trades;
	private ArrayList<City> cities;
	private ArrayList<Salesman> salespeople;

	private Connection c;
	private String url, database, driver, user, password;
	private boolean loading;

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

		loading = true;
		connect();
	}

	private synchronized void connect() {
		if (config.getBoolean("application.connection-pool")) {
			try {
				InitialContext cxt = new InitialContext();
		
				DataSource ds = (DataSource) cxt.lookup( "java:/comp/env/jdbc/postgres" );
		
				if ( ds == null ) {
				   throw new RuntimeException("Data source not found!");
				}
				c = ds.getConnection();
			} catch (NamingException e) {
				logger.fatal("Could not load data context", e);
			} catch (SQLException e) {
				logger.fatal("Database connection failed", e);
				throw new RuntimeException("Kunne ikke oprette forbindelse til databasen.");
			}
		} else {
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
	}

	private void close() {
		if (loading && !config.getBoolean("application.test-mode")) {
			logger.info("Still loading. omitting close request.");
			return;
		}
		
		try {
			logger.info("Closing database");
			c.close();
			logger.info("Connection closed");
		} catch (SQLException e) {
			logger.error("Could not close database connection", e);
		}
	}
	
	private ArrayList<String> getPhones(String table, String idColumn, int id) {
		ArrayList<String> phones = new ArrayList<String>();
		
		String query = "SELECT phone FROM "+table+" WHERE "+idColumn+"="+id;
		
		Statement phoneStatement = null;
		ResultSet phoneResults = null;
		
		try {
			connect();
			
			phoneStatement = c.createStatement();
			phoneResults = phoneStatement.executeQuery(query);
			
			while (phoneResults.next()) {
				phones.add(phoneResults.getString("phone"));
			}
		} catch (Exception e) {
			logger.fatal("Get "+table, e);
			throw new RuntimeException("Kunne ikke hente listen af telefonnumre");
		} finally {
			if (phoneResults != null) {
				try {
					phoneResults.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the result set", e);
				}
			}
			if (phoneStatement != null) {
				try {
					phoneStatement.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the statement", e);
				}
			}
		}
		
		return phones;
	}
	
	public void loaded() {
		loading = false;
		close();
	}

	public Importance getImportance(String name) {
		return Importance.valueOf(name);
	}

	public synchronized ArrayList<City> getCities() {
		if (cities == null) {
			logger.info("Preparing to fetch cities");
			
			cities = new ArrayList<City>();
			cityMap = new HashMap<Integer, City>();

			Statement cityStatement = null;
			ResultSet cityResults = null;
			try {
				connect();

				cityStatement = c.createStatement();

				String citySql = "SELECT p.postal, p.city FROM postalcodes p;";
				logger.info("Fetching cities");
				logger.debug("\tThe sql being used is: " + citySql);
				cityResults = cityStatement.executeQuery(citySql);

				while (cityResults.next()) {
					int postal = cityResults.getInt("postal");
					String cityname = cityResults.getString("city");
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
			} finally {
				if (cityResults != null) {
					try {
						cityResults.close();
					} catch (SQLException e) {
						logger.fatal("Could not close the result set", e);
					}
				}
				if (cityStatement != null) {
					try {
						cityStatement.close();
					} catch (SQLException e) {
						logger.fatal("Could not close the statement", e);
					}
				}
				close();
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

				mail.setHostName(config.getString("smtp.host"));
				mail.setSmtpPort(config.getInt("smtp.port"));
				mail.setSSL(config.getBoolean("smtp.ssl"));
				mail.setTLS(config.getBoolean("smtp.tls"));
				
				logger.debug("\tSending the mail\n\t\tSubject: "+subject+"\n\t\tMessage: "+mail
						+"\n\n\t\tTo: "+recipiant+"\n\t\tFrom: "+user+"\n");
				
				if (config.getBoolean("application.test-mode"))
					logger.info("Test mode enabled, mail is not sent.");
				else
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

		Statement companyStatement = null;
		ResultSet companyResults = null;
		try {
			connect();
			companyStatement = c.createStatement();

			String companyQuery = "SELECT DISTINCT\n"
					+ "	c.companyid,\n"
					+ "	c.companyname,\n"
					+ "	c.address,\n"
					+ "	c.postal,\n"
					+ " c.city,\n"
					+ "	c.importance,\n"
					+ "	c.comments,\n"
					+ " c.tradeid\n"
					+ "		FROM salespeople s, companieswithcities c, contacts k\n"
					+ "		WHERE c.companyid = k.companyid\n"
					+ "			AND k.salesmanid = s.salesmanid\n"
					+ "			AND s.salesmanid = " + salesmanId + ";";
			logger.debug("\tThe sql being used is: "+companyQuery);			

			companyResults = companyStatement.executeQuery(companyQuery);

			ArrayList<Company> companies = fillCompanyArrayList(companyResults);
			logger.info(companies.size() + " companies fetched");
			return companies;
		} catch (SQLException e) {
			logger.fatal("Fetch customers for "+salesman.get("salesmanid"), e);
			throw new RuntimeException("Kunne ikke hente kundelisten for "
					+ salesman.getSalesman());
		} finally {
			if (companyResults != null) {
				try {
					companyResults.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the result set", e);
				}
			}
			if (companyStatement != null) {
				try {
					companyStatement.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the statement", e);
				}
			}
			close();
		}
	}

	public Company getAppCompany() {
		Company appCompany = new Company();
		appCompany.setCompanyName(config.getString("company.name"));
		appCompany.setAddress(config.getString("company.address"));
		appCompany.setCity(config.getString("company.city"));
		appCompany.set("country", config.getString("company.country"));
		appCompany.set("phone", config.getString("company.phone"));
		appCompany.set("fax", config.getString("company.fax"));
		appCompany.set("webpage", config.getString("company.webpage"));
		appCompany.set("vat-no", config.getString("company.vat-no"));
		
		return appCompany;
	}
	
	public ArrayList<Company> searchForCompany(String searchString) {
		logger.info("Searching for " + searchString);
		getTrades();
		getCities();
		getSalesmen();
		
		String escSearchString = "%"+ searchString +"%";

		PreparedStatement companyStatement = null;
		ResultSet companyResults = null;
		try {
			connect();

			String companyQuery = "SELECT DISTINCT\n"
					+ "	c.companyid,\n"
					+ "	c.companyname,\n"
					+ "	c.address,\n"
					+ "	c.postal,\n"
					+ " c.city,\n"
					+ "	c.importance,\n"
					+ "	c.comments,\n"
					+ " c.tradeid\n"
					+ "		FROM companieswithcities c\n"
					+ "		WHERE c.companyname ILIKE ?\n"
					+ "		LIMIT 7;";
			logger.debug("\tThe sql being used is: "+companyQuery);			

			companyStatement = c.prepareStatement(companyQuery);
			companyStatement.setString(1, escSearchString);
			companyResults = companyStatement.executeQuery();

			ArrayList<Company> companies = fillCompanyArrayList(companyResults);
			logger.info(companies.size() + " companies fetched");
			
			return companies;
		} catch (SQLException e) {
			logger.fatal("Searching companies: " + searchString, e);
			throw new RuntimeException("Kunne ikke søge på " + searchString);
		} finally {
			if (companyResults != null) {
				try {
					companyResults.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the result set", e);
				}
			}
			if (companyStatement != null) {
				try {
					companyStatement.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the statement", e);
				}
			}
			close();
		}
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
			String importanceChar = companyResults.getString("importance");
			String comments = companyResults.getString("comments");
			int tradeid = companyResults.getInt("tradeid");
			
			logger.debug("\tCompany fetched:");
			logger.debug("\t\tID: " + companyid);
			logger.debug("\t\tName: " + companyName);
			logger.debug("\t\tAddress: " + companyAddress);
			logger.debug("\t\tCity (should be blank or null in most cases):" + companyCity);
			logger.debug("\t\tPostal: " + postal);
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

			c.setPhones(this.getPhones("companyPhones", "companyid", companyid));

			if (importanceChar == null)
				importanceChar = "I";
			c.setImportance(Importance.valueOf(importanceChar));

			c.setComments(comments);

			Trade noTrade = new Trade();
			try {
				CustomerCareI18n i18n = GWTI18N.create(CustomerCareI18n.class);
				noTrade.setTrade(i18n.noTradeSelected());
			} catch (IOException e) {
				logger.fatal("How can this even happen?", e);
			}
			if (!companyResults.wasNull())
				c.setTrade(tradeMap.get(new Integer(tradeid)));
			else
				c.setTrade(noTrade);
			
			companies.add(c);
			logger.debug("\t\tSuccessfully added\n");
		}
		return companies;
	}
	
	public ArrayList<Company> getProspectCompanies() {
		logger.info("Getting prospect companies");
		getTrades();
		getCities();
		getSalesmen();

		Statement companyStatement = null;
		ResultSet companyResults = null;
		try {
			connect();
			companyStatement = c.createStatement();

			String companyQuery = "SELECT DISTINCT\n"
					+ "	c.companyid,\n"
					+ "	c.companyname,\n"
					+ "	c.address,\n"
					+ "	c.postal,\n"
					+ " c.city,\n"
					+ "	c.importance,\n"
					+ "	c.comments,\n"
					+ " c.tradeid\n"
					+ "		FROM companieswithcities c\n"
					+ "		WHERE c.companyid NOT IN\n"
					+ "			(SELECT k.companyid FROM contacts k\n"
					+ "				WHERE k.salesmanid IS NOT null);";
			logger.debug("\tSql used is: " + companyQuery);

			companyResults = companyStatement.executeQuery(companyQuery);

			ArrayList<Company> companies = fillCompanyArrayList(companyResults);
			logger.info(companies.size() + " prospects fetched");
			
			return companies;
		} catch (SQLException e) {
			logger.fatal("Get prospects", e);
			throw new RuntimeException("Kunne ikke hente listen af emner.");
		} finally {
			if (companyResults != null) {
				try {
					companyResults.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the result set", e);
				}
			}
			if (companyStatement != null) {
				try {
					companyStatement.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the statement", e);
				}
			}
			close();
		}
	}
	
	public ArrayList<Company> getAllCompanies() {
		logger.info("Getting all companies");
		getTrades();
		getCities();
		getSalesmen();

		Statement companyStatement = null;
		ResultSet companyResults = null;
		try {
			connect();
			companyStatement = c.createStatement();

			String companyQuery = "SELECT DISTINCT\n"
					+ "	c.companyid,\n"
					+ "	c.companyname,\n"
					+ "	c.address,\n"
					+ "	c.postal,\n"
					+ " c.city,\n"
					+ "	c.importance,\n"
					+ "	c.comments,\n"
					+ " c.tradeid\n"
					+ "		FROM companieswithcities c\n";
			logger.debug("\tSql used is: " + companyQuery);

			companyResults = companyStatement.executeQuery(companyQuery);

			ArrayList<Company> companies = fillCompanyArrayList(companyResults);
			logger.info(companies.size() + " prospects fetched");
			return companies;
		} catch (SQLException e) {
			logger.fatal("Get all companies", e);
			throw new RuntimeException("Kunne ikke hente den komlette liste af virksomheder.");
		} finally {
			if (companyResults != null) {
				try {
					companyResults.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the result set", e);
				}
			}
			if (companyStatement != null) {
				try {
					companyStatement.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the statement", e);
				}
			}
			close();
		}
	}
	
	public synchronized Integer createCompany(Company company,
			ArrayList<Contact> contacts, Salesman salesman) {
		connect();

		CallableStatement insertProc = null;
		try {
			logger.info("Inserting company");
			String storedCall = "{? = call insertCompany " +
					"(?, ?, ?, ?, ?, ?, ?) }";
			insertProc = c.prepareCall(storedCall);
			insertProc.registerOutParameter(1, Types.INTEGER);
			
			insertProc.setString(2, company.getCompanyName());
			insertProc.setString(3, company.getAddress());
			if (company.get("postal") != null)
				insertProc.setInt(4, company.getPostal());
			else
				insertProc.setNull(4, Types.INTEGER);
			
			Array phoneArray = c.createArrayOf("varchar", company.getPhones().toArray());
			insertProc.setArray(5, phoneArray);
			
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
		} finally {
			if (insertProc != null) {
				try {
					insertProc.close();
				} catch (SQLException e) {
					logger.fatal("Could not close stored procedure", e);
				}
			}
			close();
		}
	}

	public void updateCompany(Company company) {
		connect();

		CallableStatement updateProc = null;
		try {
			logger.info("Updating company");
			String storedCall = "{call updateCompany " +
					"(?, ?, ?, ?, ?, ?, ?, ?) }";
			
			Array phoneArray = c.createArrayOf("varchar", company.getPhones().toArray());
			
			Trade trade = company.getTrade();
			Integer tradeid = null;
			if (trade != null)
				tradeid = trade.get("tradeid");
			
			updateProc = c.prepareCall(storedCall);
			updateProc.setInt(1, (Integer) company.get("companyid"));
			updateProc.setString(2, company.getCompanyName());
			updateProc.setString(3, company.getAddress());
			if (company.getPostal() == 0)
				updateProc.setNull(4, Types.INTEGER);
			else
				updateProc.setInt(4, company.getPostal());
			updateProc.setArray(5, phoneArray);
			if (tradeid == null)
				updateProc.setNull(6, Types.INTEGER);
			else
				updateProc.setInt(6, tradeid);
			updateProc.setString(7, company.getImportance().name());
			updateProc.setString(8, company.getComments());
			
			updateProc.execute();
			logger.info("\tSuccessfully updated");
			updateProc.close();
		} catch (Exception e) {
			logger.fatal("Update company: "+company.get("companyid"), e);
			throw new RuntimeException("Kunne ikke opdatere: "+company.getCompanyName());
		} finally {
			if (updateProc != null) {
				try {
					updateProc.close();
				} catch (SQLException e) {
					logger.fatal("Could not close stored procedure", e);
				}
			}
			close();
		}
	}

	public synchronized void deleteCompanies(List<Company> companies) {
		logger.info("Delete companies");
		for (Company c : companies)
			deleteCompany(c);
	}

	public void deleteCompany(Company company) {
		connect();

		CallableStatement deleteProc = null;
		try {
			logger.info("Deleting company");
			String storedCall = "{call deleteCompany ( ? ) }";
			
			deleteProc = c.prepareCall(storedCall);
			deleteProc.setInt(1, (Integer) company.get("companyid"));
			
			deleteProc.execute();
			logger.info("\tSuccessfully deleted company");
			deleteProc.close();
		} catch (Exception e) {
			logger.fatal("Delete company "+company.get("companyid"), e);
			throw new RuntimeException("Kunne ikke slette: "+company.getCompanyName());
		} finally {
			if (deleteProc != null) {
				try {
					deleteProc.close();
				} catch (SQLException e) {
					logger.fatal("Could not close stored procedure", e);
				}
			}
			close();
		}
	}
	
	public Company getCompanyFor(Contact contact) {
		logger.info("Starting to find company");
		Company company = null;
		
		Statement companyStatement = null;
		ResultSet companyResults = null;
		try {
			connect();
			companyStatement = c.createStatement();

			String companyQuery = "SELECT DISTINCT\n"
					+ "	c.companyid,\n"
					+ "	c.companyname,\n"
					+ "	c.address,\n"
					+ "	c.postal,\n"
					+ " c.city,\n"
					+ "	c.importance,\n"
					+ "	c.comments,\n"
					+ " c.tradeid\n"
					+ "		FROM companieswithcities c, contacts k\n"
					+ "		WHERE c.companyid = k.companyid\n"
					+ "			AND k.contactid = " + contact.get("contactid") + ";";
			logger.debug("\tThe sql being used is: "+companyQuery);			

			companyResults = companyStatement.executeQuery(companyQuery);
			logger.info("\tCompanies fetched");
			
			companyResults.close();
			companyStatement.close();

			ArrayList<Company> companies = fillCompanyArrayList(companyResults);
			if (companies.size() == 1) {
				logger.debug("\t1 company in the list, setting the company for return");
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
		} finally {
			if (companyResults != null) {
				try {
					companyResults.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the result set", e);
				}
			}
			if (companyStatement != null) {
				try {
					companyStatement.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the statement", e);
				}
			}
			close();
		}
		
		return company;
	}
	
	//
	// Contacts
	//
	public int insertContact(Contact contact, int salesmanid, int companyid) {
		connect();
		
		CallableStatement insertProc = null;
		try {
			String storedCall = "{? = call insertContact " +
				"(?, ?, ?, ?, ?, ?, ?, ?) }";
			
			Array phoneArray = c.createArrayOf("VARCHAR(128)", contact.getPhones().toArray());
			
			insertProc = c.prepareCall(storedCall);
			insertProc.registerOutParameter(1, Types.INTEGER);
			
			insertProc.setInt(2, companyid);
			insertProc.setInt(3, salesmanid);
			
			String contactName = "";
			String title = "";
			String mail = "";
			Boolean acceptsmails = false;
			String comments = "";
			
			if (contact.getName() != null)
				contactName = contact.getName();
			if (contact.getTitle() != null)
				title = contact.getTitle();
			if (contact.getMail() != null)
				mail = contact.getMail();
			if (contact.getAcceptsMails() != null)
				acceptsmails = contact.getAcceptsMails();
			if (contact.getComments() != null)
				comments = contact.getComments();
			
			insertProc.setString(4, contactName);
			insertProc.setString(5, title);
			insertProc.setArray(6, phoneArray);
			insertProc.setString(7, mail);
			insertProc.setBoolean(8, acceptsmails);
			insertProc.setString(9, comments);
			
			insertProc.execute();
			int contactId = insertProc.getInt(1);
			
			return contactId;
		} catch (Exception e) {
			logger.fatal("Insert contact", e);
			throw new RuntimeException("Kunne ikke oprette kontaktpersonen: "+contact.getName());
		} finally {
			if (insertProc != null) {
				try {
					insertProc.close();
				} catch (SQLException e) {
					logger.fatal("Could not closed store procedure", e);
				}
			}
			close();
		}
	}
	
	public void updateContact(Contact contact) {
		connect();

		CallableStatement updateProc = null;
		try {
			String storedCall = "{call updateContact " +
					"(?, ?, ?, ?, ?, ?, ?, ?) }";
			Array phoneArray = c.createArrayOf("VARCHAR(128)", contact.getPhones().toArray());
			
			updateProc = c.prepareCall(storedCall);
			updateProc.setInt(1, (Integer) contact.get("contactid"));
			updateProc.setInt(2, (Integer) contact.getSalesman().get("salesmanid"));
			updateProc.setString(3, contact.getName());
			updateProc.setString(4, contact.getTitle());
			updateProc.setArray(5, phoneArray);
			updateProc.setString(6, contact.getMail());
			updateProc.setBoolean(7, contact.getAcceptsMails());
			updateProc.setString(8, contact.getComments());
			
			updateProc.execute();
		} catch (Exception e) {
			logger.fatal("Update contact "+contact.get("contactid"), e);
			throw new RuntimeException("Kunne ikke opdatere kontaktpersonen: "+contact.getName());
		} finally {
			if (updateProc != null) {
				try {
					updateProc.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the stored procedure", e);
				}
			}
			close();
		}
	}
	
	public synchronized void deleteContacts(List<Contact> contacts) {
		for (Contact c : contacts)
			deleteContact(c);
	}
	
	public void deleteContact(Contact contact) {
		connect();

		CallableStatement deleteProc = null;
		try {
			String storedCall = "{call deleteContact ( ? ) }";
			
			deleteProc = c.prepareCall(storedCall);
			deleteProc.setInt(1, (Integer) contact.get("contactid"));
			
			deleteProc.execute();
		} catch (Exception e) {
			logger.fatal("Delete contact "+contact.get("contactid"), e);
			throw new RuntimeException("Kunne ikke slette kontakten: "+contact.getName());
		} finally {
			if (deleteProc != null) {
				try {
					deleteProc.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the stored procedure", e);
				}
			}
			
			close();
		}
	}

	public synchronized ArrayList<Contact> getContactsFor(Salesman salesman,
			Company company) {
		Statement contactStatement = null;
		ResultSet contactResult = null;
		try {
			String contactQuery = "SELECT\n" +
				"\tk.contactid,\n" +
				"\tk.contactname,\n" +
				"\tk.title,\n" +
				"\tk.mail,\n" +
				"\tk.acceptsmails,\n" +
				"\tk.comments\n" +
				"\t\tFROM contacts k\n" +
				"\t\tWHERE k.companyid = " + company.get("companyid") + "\n";
			
			if (salesman != null)
				contactQuery += "\t\tAND k.salesmanid = " + salesman.get("salesmanid") + ";";
			else
				contactQuery += "\t\tAND k.salesmanid IS NULL;";
			
			logger.debug("SQL string for fetching contacts is: \n"+contactQuery);

			connect();
			contactStatement = c.createStatement();
			contactResult = contactStatement
					.executeQuery(contactQuery);

			ArrayList<Contact> contacts = new ArrayList<Contact>();
			while (contactResult.next()) {
				Contact k = new Contact();
				k.set("contactid", contactResult.getInt("contactid"));
				k.setName(contactResult.getString("contactname"));
				k.setTitle(contactResult.getString("title"));
				k.setPhones(this.getPhones("contactphones", "contactid",
						contactResult.getInt("contactid")));
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
		} finally {
			if (contactResult != null) {
				try {
					contactResult.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the result set", e);
				}
			}
			if (contactStatement != null) {
				try {
					contactStatement.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the statement", e);
				}
			}
			close();
		}
	}

	public ArrayList<Contact> getAllContacts(Company company) {
		Statement contactStatement = null;
		ResultSet contactResult = null;
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
			contactStatement = c.createStatement();
			contactResult = contactStatement
					.executeQuery(contactQuery);

			ArrayList<Contact> contacts = new ArrayList<Contact>();
			while (contactResult.next()) {
				Contact k = new Contact();
				k.set("contactid", contactResult.getInt("contactid"));
				k.setName(contactResult.getString("contactname"));
				k.setTitle(contactResult.getString("title"));
				k.setPhones(this.getPhones("contactphones", "contactid",
						contactResult.getInt("contactid")));
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
		} finally {
			if (contactResult != null) {
				try {
					contactResult.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the result set", e);
				}
			}
			if (contactStatement != null) {
				try {
					contactStatement.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the statement", e);
				}
			}
			close();
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

			Statement salespeopleStatement = null;
			ResultSet salespeopleResult = null;
			
			try {
				connect();

				salespeopleStatement = c.createStatement();
				salespeopleResult = salespeopleStatement
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
			} finally {
				if (salespeopleResult != null) {
					try {
						salespeopleResult.close();
					} catch (SQLException e) {
						logger.fatal("Could not close the result set", e);
					}
				}
				if (salespeopleStatement != null) {
					try {
						salespeopleStatement.close();
					} catch (SQLException e) {
						logger.fatal("Could not close the statement", e);
					}
				}
				close();
			}
		}

		return salespeople;
	}
	
	public int insertSalesman(Salesman salesman) {
		connect();
		
		CallableStatement insertProc = null;
		try {
			String storedCall = "{? = call insertSalesman " +
			"(?, ?, ?, ?) }";
			insertProc = c.prepareCall(storedCall);
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
		}  finally {
			if (insertProc != null) {
				try {
					insertProc.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the stored procedure", e);
				}
			}
			close();

		}
	}
	
	public void updateSalesman(Salesman salesman) {
		connect();

		CallableStatement updateProc = null;
		try {
			String storedCall = "{call updateSalesman " +
					"(?, ?, ?, ?, ?) }";
			
			updateProc = c.prepareCall(storedCall);
			updateProc.setInt(1, (Integer) salesman.get("salesmanid"));
			updateProc.setString(2, salesman.getSalesman());
			updateProc.setString(3, salesman.getTitle());
			updateProc.setString(4, salesman.getPhone());
			updateProc.setString(5, salesman.getMail());
			
			updateProc.execute();
			
			for (Salesman s : salespeople) {
				if (s.get("salesmanid").equals(salesman.get("salesmanid"))) {
					s.setProperties(salesman.getProperties());
					break;
				}
			}
				
		} catch (Exception e) {
			logger.fatal("Update salesman "+salesman.get("salesmanid"), e);
			throw new RuntimeException("Kunne ikke opdatere sælgeren: "+salesman.getSalesman());
		} finally {
			if (updateProc != null) {
				try {
					updateProc.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the stored procedure", e);
				}
			}
			close();
		}
	}
	
	public void deleteSalesman(Salesman salesman) {
		connect();

		CallableStatement deleteProc = null;
		try {
			String storedCall = "{call deleteSalesman ( ? ) }";
			
			deleteProc = c.prepareCall(storedCall);
			deleteProc.setInt(1, (Integer) salesman.get("salesmanid"));
			
			deleteProc.execute();
			salespeople.remove(salesman);
		} catch (Exception e) {
			logger.fatal("Delete salesman "+salesman.get("contactid"), e);
			throw new RuntimeException("Kunne ikke slette sælgeren: "+salesman.getSalesman());
		} finally {
			if (deleteProc != null) {
				try {
					deleteProc.close();
				} catch (SQLException e) {
					logger.fatal("Could not close stored procedure", e);
				}
			}
			close();
		}
	}
	
	//
	// Trades
	//
	public synchronized ArrayList<Trade> getTrades() {
		tradeMap = new HashMap<Integer, Trade>();
		trades = new ArrayList<Trade>();

		String tradeSql = "SELECT t.tradeid, t.tradename FROM trade t";

		Statement tradeStatement = null;
		ResultSet tradeResult = null;
		try {
			connect();

			tradeStatement = c.createStatement();
			tradeResult = tradeStatement.executeQuery(tradeSql);

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
		} finally {
			if (tradeResult != null) {
				try {
					tradeResult.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the result set", e);
				}
			}
			if (tradeStatement != null) {
				try {
					tradeStatement.close();
				} catch (SQLException e) {
					logger.fatal("Could not close the statement", e);
				}
			}
			close();
		}
		return trades;
	}

	public void addTrade(Trade trade) {
		connect();
		
		CallableStatement insertProc = null;
		try {
			String storedCall = "{call insertTrade (?, ?) }";
			insertProc = c.prepareCall(storedCall);
			
			insertProc.setInt(1, trade.getId());
			insertProc.setString(2, trade.getTrade());
			
			insertProc.execute();
		} catch (Exception e) {
			logger.fatal("Insert trade", e);
			throw new RuntimeException("Kunne ikke oprette branchen: "+trade.getTrade());
		} finally {
			if (insertProc != null) {
				try {
					insertProc.close();
				} catch (SQLException e) {
					logger.fatal("Could not close stored procedure", e);
				}
			}
			close();
		}
	}
	
	public void deleteTrade(Trade trade) {
		connect();
		
		CallableStatement deleteProc = null; 
		try {
			String storedCall = "{call deleteTrade (?) }";
			deleteProc = c.prepareCall(storedCall);
			
			deleteProc.setInt(1, trade.getId());
			
			deleteProc.execute();
		} catch (Exception e) {
			logger.fatal("Delete trade", e);
			throw new RuntimeException("Kunne ikke slette branchen: "+trade.getTrade());
		} finally {
			if (deleteProc != null) {
				try {
					deleteProc.close();
				} catch (SQLException e) {
					logger.fatal("Could not close stored procedure", e);
				}
			}
			close();
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
		
		CallableStatement batchIdProc = null;
		CallableStatement companyProc = null; 
		CallableStatement contactProc = null;
		try {
			
			String batchIdCall = "{ ? = call nextval('labelqueue_batchid_seq') }";
			batchIdProc = c.prepareCall(batchIdCall);
			
			batchIdProc.registerOutParameter(1, Types.BIGINT);
			batchIdProc.execute();
			batchid = (int) batchIdProc.getLong(1);
			
			String storedCall = "{ call labelQueueAddCompany (?, ?) }";
			companyProc = c.prepareCall(storedCall);			
			companyProc.setInt(1, batchid);
			
			storedCall = "{ call labelQueueAddContact (?, ?) }";
			contactProc = c.prepareCall(storedCall);
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
		} catch (Exception e) {
			logger.fatal("Trying to queue company for mail", e);
			throw new RuntimeException("Kunne ikke oprette post-data.");
		} finally {
			if (batchIdProc != null) {
				try {
					batchIdProc.close();
				} catch (SQLException e) {
					logger.fatal("Could not close stored procedure", e);
				}
			}
			if (companyProc != null) {
				try {
					companyProc.close();
				} catch (SQLException e) {
					logger.fatal("Could not close stored procedure", e);
				}
			}
			if (contactProc != null) {
				try {
					contactProc.close();
				} catch (SQLException e) {
					logger.fatal("Could not close stored procedure", e);
				}
			}
			close();
		}
		
		return batchid;
	}
}