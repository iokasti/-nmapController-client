package threads;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import device.DeviceInfo;
import nmapJob.Job;
import softwareAgent.Main;
import softwareAgent.Properties;

/**
 * The RequestJobs class used to request jobs from AM.
 * 
 * @author Konstantinos Dalianis
 * @author Maria Fava
 * @version 2.0
 * @since 2015-12-31
 */
public class RequestJobs implements Runnable {
	String serverAddr;
	int jobRequestInterval;
	int connectionFailureSleepTime;

	/**
	 * @param serverAddr
	 * @param jobRequestInterval
	 */
	public RequestJobs() {
		/** GET INFO FROM PROPERTIES **/
		this.serverAddr = Properties.serverAddr;
		this.jobRequestInterval = Properties.jobRequestInterval;
		this.connectionFailureSleepTime = Properties.connectionFailureSleepTime;
		/************************************************/
	}

	public void run() {
		System.err.println("Started Request Jobs Thread: " + Thread.currentThread().getId());

		/** CREATE THE CLIENT **/
		Client client = new Client();
		WebResource webResource = client.resource(serverAddr + "/requests/post/");
		/************************************************/

		/**
		 * GET HASH IN JSON FORMAT.
		 **/
		String deviceHashJSON = "{\"hash\":\"" + Integer.toString(DeviceInfo.hash) + "\"}";

		while (Thread.currentThread().isInterrupted() == false) {
			// repeat until shutdownhook interrupts the process

			/** SEND THE REQUEST AND GET A RESPONSE **/
			ClientResponse response = null;
			try {
				response = webResource.accept("application/json").type("application/json").post(ClientResponse.class,
						deviceHashJSON);
			} catch (Exception e) {
			}
			/*****************************************/

			if (response != null && response.getStatus() == 200) {
				/** RESPONSE WAS NORMALLY RECEIVED **/
				response.bufferEntity();
				String respond = response.getEntity(String.class);

				if (respond.equals("nojobs") == false) {
					// List with jobs was send from the AM
					/** PARSE THE JSON LIST **/
					Object obj = JSONValue.parse(respond);
					JSONArray array = (JSONArray) obj;
					for (int i = 0; i < array.size(); i++) {
						// this json object is a job from the list
						JSONObject jobJSON = (JSONObject) array.get(i);	
						
						Job job = new Job(Integer.parseInt(jobJSON.get("id").toString()),
								jobJSON.get("parameters").toString(),
								Boolean.parseBoolean(jobJSON.get("periodic").toString()),
								Integer.parseInt(jobJSON.get("time").toString()));
						
						if (job.getId() == -1 && job.getParameters().equals("exit(0)") && job.isPeriodic() == true
								&& job.getTime() == -1) {
							// AM signaled SA to terminate
							// we must call the shutdown hook,
							// but calling it here won't allow us to
							// join the requests jobs thread
							// so make a new runnable thread that does
							// system exit and therefore it calls
							// the shutdown hook
							new Thread(new Runnable() {
								public void run() {
									System.exit(0);
								}
							}).start();
						} else if (job.getParameters().equals("Stop") && job.isPeriodic() == true
								&& job.getTime() == -1) {
							// AM signaled to delete periodic job with
							// specific id
							Thread threadToBeInterrupted = Main.periodicJobThreadPool.get(job.getId());
							if (threadToBeInterrupted != null) {
								threadToBeInterrupted.interrupt();
								try {
									threadToBeInterrupted.join(1500);
								} catch (InterruptedException e) {
									// end
									System.err.println(
											"Terminated Request Jobs Thread: " + Thread.currentThread().getId());
									return;
								}
							}
							Main.periodicJobThreadPool.remove(job.getId());
						} else if (job.isPeriodic() == false) {
							// add to one time jobs list
							Main.oneTimeJobs.put(job);
						} else if (job.isPeriodic() == true) {
							// create periodic job thread, add it to the
							// periodic job thread pool using for key
							// the job id
							PeriodicJob periodicJob = new PeriodicJob(job);
							Thread periodicJobThread = new Thread(periodicJob);
							periodicJobThread.start();
							Main.periodicJobThreadPool.put(Integer.valueOf(job.getId()), periodicJobThread);
						}
					}
					/******************************************/
				}
				try {
					Thread.sleep(jobRequestInterval * 1000);
				} catch (InterruptedException e) {
					// end
					System.err.println("Terminated Request Jobs Thread: " + Thread.currentThread().getId());
					return;
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
					Thread.sleep(connectionFailureSleepTime * 1000);
				} catch (InterruptedException e) {
				}
				/*****************************************/
			}
		}
		// end
		System.err.println("Terminated Request Jobs Thread: " + Thread.currentThread().getId());
		return;
	}
}
