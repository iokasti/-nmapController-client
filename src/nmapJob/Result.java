package nmapJob;

import org.apache.commons.lang3.StringEscapeUtils;

import device.DeviceInfo;

/**
 * The Result class includes a Job and the corresponding result of the nmap
 * command.
 * 
 * @author Konstantinos Dalianis
 * @author Maria Fava
 * @version 2.0
 * @since 2015-11-19
 */
public class Result {
	/**
	 * hash of this agent.
	 */
	private int hash;

	/**
	 * Result of the job.
	 */
	private String result;

	/**
	 * Job information.
	 */
	private Job job;

	/**
	 * Constructor or Result.
	 * 
	 * @param result
	 *            Job results from nmap.
	 * @param job
	 *            Job information.
	 */
	public Result(String result, Job job) {
		this.hash = DeviceInfo.hash;
		try {
			this.result = escapeXml(result);
		} catch (Exception e) {
			System.err.println("Could not escape XML");
		}
		this.job = job;
	}

	/**
	 * @return Result as json.
	 */
	@Override
	public String toString() {
		return "{\"hash\":\"" + hash + "\",\"jobID\":\"" + job.getId() + "\",\"result\":\"" + result + "\"}";
	}

	/**
	 * Used to escape xml results so we can later send them in json to the
	 * aggregator manager.
	 * 
	 * @param xml
	 * @return Given xml, escaped from special characters like "" etc.
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public String escapeXml(String xml) throws Exception {
		return StringEscapeUtils.escapeXml(xml);
	}

}
