package softwareAgent;

import java.util.concurrent.TimeUnit;

import org.json.simple.JSONValue;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import device.DeviceInfo;

/**
 * The Register class sends post requests to the server and awaits to be
 * accepted.
 * 
 * @author Konstantinos Dalianis
 * @author Maria Fava
 * @version 2.0
 * @since 2015-12-31
 */
public abstract class Register {

	/**
	 * Sends registration requests the the aggregator manager until registered,
	 * or cancelled.
	 */
	public static void register() {
		/** GET SERVER CONNECTION INFO FROM PROPERTIES **/
		String serverAddr = Properties.serverAddr;
		int connectionFailureSleepTime = Properties.connectionFailureSleepTime;
		/************************************************/

		/** CREATE THE CLIENT **/
		Client client = new Client();
		WebResource webResource = client.resource(serverAddr + "/register/post/");
		/************************************************/

		/**
		 * GET DEVICE INFORMATION IN JSON FORMAT (new operator is used because
		 * toJSONString functions needs to use class Override function
		 * toString())
		 **/
		String deviceInfoJSON = JSONValue.toJSONString(new DeviceInfo());

		do {
			/** SEND THE REQUEST AND GET A RESPONSE **/
			ClientResponse response = null;
			try {
				response = webResource.accept("application/json").type("application/json").post(ClientResponse.class,
						deviceInfoJSON);
			} catch (Exception e) {
			}
			/*****************************************/

			if (response != null && response.getStatus() == 200) {
				/** RESPONSE WAS NORMALLY RECEIVED **/
				response.bufferEntity();
				String respond = response.getEntity(String.class);

				if (respond.equals("activated")) {
					// SA was accepted so break from loop
					System.err.println("Agent is now activated.");
					Main.canceled = false;
					break;
				} else if (respond.equals("wait")) {
					// SA was ordered to wait
					System.err.println("Agent awaits to be accepted.");
				} else if (respond.equals("cancel")) {
					// SA was ordered to terminate
					System.err.println("Agent was canceled.");
					Main.canceled = true;
					System.exit(1);
				} else if (respond.equals("error")) {
					System.err.println("Something went wrong.");
				}
				// wait for connectionFailureSleepTime seconds
				// and resend the post
				try {
					TimeUnit.SECONDS.sleep(connectionFailureSleepTime);
				} catch (InterruptedException e) {
				}
				/*****************************************/
			} else {
				/** RESPONSE WASN'T NORMALLY RECEIVED - RETRY **/
				if (response != null) {
					System.err.println("Connection to server failed : HTTP error code : " + response.getStatus());
				} else {
					System.err.println("Connection to server failed : Server is probably down.");
				}
				try {
					TimeUnit.SECONDS.sleep(connectionFailureSleepTime);
				} catch (InterruptedException e) {
				}
				/*****************************************/
			}

		} while (true);
	}
}
