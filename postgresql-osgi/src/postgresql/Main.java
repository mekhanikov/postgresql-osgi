package postgresql;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.ConfigurationPolicy;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.log.LogService;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

@Component(name = "postgresql-osgi",
// Bring this service up even if nobody is using it
immediate = true
// Require configuration to be present to enable this service
// Configuration file will be named ${componentName}.cfg in the etc/ directory
//,configurationPolicy = ConfigurationPolicy.require
)
public class Main {

	private DataSourceFactory dataSourceFactory;

	private DataSource dataSource;

	static LogService logService;
	
	public Main() {
		System.out.println("*** Main()");
	}

	@Activate
	public void activate(Map<String, String> properties) {
		logService.log(LogService.LOG_INFO, "activating service");
		Properties dbProps = new Properties();
		dbProps.put(DataSourceFactory.JDBC_DATABASE_NAME, properties.get("dbname"));
		dbProps.put(DataSourceFactory.JDBC_USER, properties.get("user"));
		dbProps.put(DataSourceFactory.JDBC_PASSWORD, properties.get("password"));
		dbProps.put(DataSourceFactory.JDBC_SERVER_NAME, properties.get("server"));
		dbProps.put(DataSourceFactory.JDBC_PORT_NUMBER, properties.get("port"));
		try {
			dataSource = dataSourceFactory.createDataSource(dbProps);
			// Or
			// dataSourceFactory.createConnectionPoolDataSource(dbProps);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		executeSQL(true, "5555555555", "foo");
		logService.log(LogService.LOG_INFO, "Added stuff");
	}

	@Deactivate
	public void deactivate() {

	}

	// Reference will get injected in OSGi with the service that implements this interface
	// The target is optional...but if you really want to enforce that this will
	// go to a PostgreSQL database you can do that like this
	@Reference(target = "(osgi.jdbc.driver.name=postgresql)")
	public void setDataSourceFactory(DataSourceFactory factory) {
		this.dataSourceFactory = factory;
	}

	public void unsetDataSourceFactory(DataSourceFactory factory) {
		this.dataSourceFactory = null;
	}

	private boolean executeSQL(boolean forward, String number, String username)
	{
		Connection c = null;
		try
		{
			c = dataSource.getConnection();
			PreparedStatement statementCreate = c.prepareStatement("CREATE TABLE IF NOT EXISTS films ( title varchar(40), year  integer);");
			statementCreate.execute();
			PreparedStatement statement = c
					.prepareStatement("INSERT INTO films (title, year) VALUES (?, ?)");
			statement.setString(1, "ssimpsons");
			statement.setInt(2, 2014);
			statement.execute();
			System.out.println("Success");
		}
		catch (SQLException e)
		{
			logService.log(LogService.LOG_ERROR, "Unable to make database modifications...", e);
			return false;
		}
		finally
		{
			try
			{
				c.close();
			}
			catch (Exception e)
			{
			}
		}
		return true;
	}

	@Reference
	void setLogService(LogService logService) {
		this.logService = logService;
	}
}