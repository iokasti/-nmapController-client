package softwareAgent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * The Properties class saves the properties for running the SA. We may have
 * more properties in the future so this class may be extended.
 * 
 * @author Konstantinos Dalianis
 * @author Maria Fava
 * @version 2.0
 * @since 2015-11-19
 */
public class Properties {
	/**
	 * Number of threads initialized for the oneTimeJob thread pool.
	 */
	public static int threadPoolSize;

	/**
	 * Address of server for register.
	 */
	public static String serverAddr;

	/**
	 * Time of seconds to sleep if connection to server fails.
	 */
	public static int connectionFailureSleepTime;

	/**
	 * Time of seconds to sleep between job requests
	 */
	public static int jobRequestInterval;

	/**
	 * Time of seconds to sleep between job requests
	 */
	public static int resultSendInterval;

	/**
	 * Constructor of Properties. Saves all the properties read from the file in
	 * class variables.
	 * 
	 * @param propertyFile
	 *            Name of file including properties.
	 * @throws IOException
	 */
	public static void setProperties(String propertyFile) throws IOException {
		BufferedReader property = new BufferedReader(new FileReader(propertyFile));
		String line;
		while ((line = property.readLine()) != null) {
			String[] propertyInfo = line.split(" ");
			if (propertyInfo[0].equals("threadpoolsize")) {
				threadPoolSize = Integer.parseInt(propertyInfo[1]);
			} else if (propertyInfo[0].equals("serverAddr")) {
				serverAddr = propertyInfo[1];
			} else if (propertyInfo[0].equals("connectionFailureSleepTime")) {
				connectionFailureSleepTime = Integer.parseInt(propertyInfo[1]);
			} else if (propertyInfo[0].equals("jobRequestInterval")) {
				jobRequestInterval = Integer.parseInt(propertyInfo[1]);
			} else if (propertyInfo[0].equals("resultSendInterval")) {
				resultSendInterval = Integer.parseInt(propertyInfo[1]);
			}
		}
		property.close();
	}
}
