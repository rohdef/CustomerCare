package dk.rohdef.server;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;


public class PdfServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger(PdfServlet.class);
	
	private PdfPTable createTable(List<LabelData> companies) {
		logger.debug("Creating PDF table for " + companies.size() + " companies");
		
		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(100f);
		float cellHeight = PageSize.A4.getHeight()/4;
		table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
		
		for (LabelData c : companies) {
			logger.debug("\tAdding the company " + c.company + " to the PDF.");
			PdfPCell cell = new PdfPCell();
			cell.setBorder(0);
			cell.setPadding(5f);
			cell.setPaddingTop(75f);
			cell.setPaddingLeft(90f);
			
			Phrase phrase = new Phrase(c.company);
			cell.addElement(phrase);
			phrase = new Phrase(c.address);
			cell.addElement(phrase);
			phrase = new Phrase(c.city);
			cell.addElement(phrase);
			if (c.attention != null) {
				phrase = new Phrase("Att.: "+c.attention);
				cell.addElement(phrase);
			}
			
			logger.debug("\tCreated the phrase for the company.");
			
			cell.setFixedHeight(cellHeight);
			table.addCell(cell);
			
			logger.debug("\tAdded the company " + c.company);
		}
		
		if (companies.size() % 2 != 0) {
			PdfPCell cell = new PdfPCell();
			cell.setBorder(0);
			table.addCell(cell);
		}

		return table;
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doGet(req, resp);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String id = (String) req.getParameter("labelsessid");
		if (id == null)
			throw new NullPointerException("The id is set wrong");
		
		connect();
		
		try {
			OutputStream str = resp.getOutputStream();
			List<LabelData> companies = getCompaniesFor(Integer.parseInt(id.trim()));
			
			Document doc = new Document(PageSize.A4, 0, 0, 0, 0);
			PdfWriter.getInstance(doc, str);
		
			doc.open();
			doc.add(createTable(companies));
			doc.addCreator("Rohde Fischer's CustomerCare");
			doc.addCreationDate();
			doc.addTitle("Labels til print");
			doc.close();
			str.flush();
		} catch (DocumentException e) {
			logger.error("PDF document problem", e);
		} catch (IOException e) {
			logger.error("Stream problem", e);
		} catch (NumberFormatException e) {
			logger.error("The parameter isn't a number", e);
		}
	}
	
	private Connection c;
	private String url,
			database,
			driver,
			user,
			password;
	
	private List<LabelData> getCompaniesFor(int batchId) {
		List<LabelData> labels = new ArrayList<LabelData>();
		
		try {
			Statement companyStatement;
			companyStatement = c.createStatement();

			String companyQuery = "SELECT c.companyname, c.address, c.postal, p.city, \n" +
					"\tk.contactname\n" +
					"\tFROM labelqueue q, contacts k, companies c, postalcodes p\n" +
					"\tWHERE q.companyid = c.companyid AND q.contactid = k.contactid " +
					"\tAND c.postal=p.postal AND q.batchid="+batchId+";";
			
			logger.debug("Running the following query:\n" + companyQuery);
			
			ResultSet companyResults;
			companyResults = companyStatement.executeQuery(companyQuery);

			while (companyResults.next()) {
				LabelData ld = new LabelData();
				ld.company = companyResults.getString("companyname");
				ld.address = companyResults.getString("address");
				ld.city = companyResults.getInt("postal") + " "
					+ companyResults.getString("city");
				ld.attention = companyResults.getString("contactname");
				
				logger.debug("Adding the label for: " + ld.company +
						" and the att.: " + ld.attention);
				
				labels.add(ld);
			}
		} catch (Exception e) {
			logger.fatal("Trying to insert company", e);
			throw new RuntimeException("Kunne ikke hente labels med kunde.");
		}
		
		try {
			Statement companyStatement;
			companyStatement = c.createStatement();

			String companyQuery = "SELECT c.companyname, c.address, c.postal, p.city\n" +
					"\tFROM labelqueue q, companies c, postalcodes p\n" +
					"\tWHERE q.companyid = c.companyid AND q.contactid IS NULL " +
					"\tAND c.postal=p.postal AND batchid="+batchId+";";
			logger.debug("Running the following query:\n" + companyQuery);
			
			ResultSet companyResults;
			companyResults = companyStatement.executeQuery(companyQuery);

			while (companyResults.next()) {
				LabelData ld = new LabelData();
				ld.company = companyResults.getString("companyname");
				ld.address = companyResults.getString("address");
				ld.city = companyResults.getInt("postal") + " "
					+ companyResults.getString("city");
				ld.attention = null;
				
				logger.debug("Adding the label for: " + ld.company);
				
				labels.add(ld);
			}
		} catch (Exception e) {
			logger.fatal("Getting queue", e);
			throw new RuntimeException("Kunne ikke hente labels uden kunde");
		}
		
		return labels;
	}

	private void connect() {
		try {
			XMLConfiguration config = new XMLConfiguration("config.xml");
			
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
		
		try {
			c = DriverManager.getConnection(url + database, user, password);
			logger.info("Connected to database");
		} catch (SQLException e) {
			logger.fatal("Database connection failed", e);
			throw new RuntimeException("Kunne ikke oprette forbindelse til databasen.");
		}
	}
	
	private class LabelData {
		public String company, address, city, attention;
	}
}
