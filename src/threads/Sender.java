package threads;

import org.json.simple.JSONValue;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import softwareAgent.Main;
import softwareAgent.Properties;

/**
 * The Sender class represents a Sender thread.
 * 
 * @author Konstantinos Dalianis
 * @author Maria Fava
 * @version 2.0
 * @since 2015-11-19
 */
public class Sender implements Runnable {

	/**
	 * Sender thread function. Gets Results from the SharedResultQueue. Sends
	 * the result to the aggregator manager.
	 */
	public void run() {
		System.err.println("Started Sender Thread: " + Thread.currentThread().getId());

		/** GET SERVER CONNECTION INFO FROM PROPERTIES **/
		String serverAddr = Properties.serverAddr;
		int connectionFailureSleepTime = Properties.connectionFailureSleepTime;
		/************************************************/

		/** CREATE THE CLIENT **/
		Client client = new Client();
		WebResource webResource = client.resource(serverAddr + "/results/post/");
		/************************************************/

		while (Thread.currentThread().isInterrupted() == false) {
			// repeat until shutdownhook interrupts the process
			try {

				/**
				 * GET RESULTS IN LIST AND CONVERT THE LIST TO JSON FORMAT
				 **/
				String resultsJson = JSONValue.toJSONString(Main.results.getAllResults());

				do {
					// repeat until succesful message is received
					ClientResponse response = null;

					/** SEND THE REQUEST AND GET A RESPONSE **/
					try {
						response = webResource.accept("application/json").type("application/json")
								.post(ClientResponse.class, resultsJson);
					} catch (Exception e) {
						continue;
					}
					/*****************************************/

					if (response != null && response.getStatus() == 200) {
						/** RESPONSE WAS NORMALLY RECEIVED **/
						response.bufferEntity();
						String respond = response.getEntity(String.class);
						if (respond.equals("successful") == true) {
							break;
						}
						/*****************************************/
					} else {
						/** RESPONSE WASN'T NORMALLY RECEIVED - RETRY **/
						if (response != null) {
							System.err
									.println("Connection to server failed : HTTP error code : " + response.getStatus());
						} else {
							System.err.println("Connection to server failed : Server is probably down.");
						}
						try {
							Thread.sleep(connectionFailureSleepTime * 1000);
						} catch (InterruptedException e) {
						}
						/*****************************************/
					}
					// wait for resultSendInterval seconds
					Thread.sleep(Properties.resultSendInterval * 1000);
				} while (true);
			} catch (InterruptedException e) {
				// end
				System.err.println("Terminated Sender Thread: " + Thread.currentThread().getId());
				return;
			}
			if (Thread.currentThread().isInterrupted() == true) {
				// end
				System.err.println("Terminated Sender Thread: " + Thread.currentThread().getId());
				return;
			}
		}
	}
}
