package as.markon.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseService {
	private Connection c;
	
	public DatabaseService() {
		try {
			c = DriverManager.getConnection("jdbc:postgresql://localhost/Markon", "markon", "123");
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
