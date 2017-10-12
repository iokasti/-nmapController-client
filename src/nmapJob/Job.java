package nmapJob;

/**
 * The Job class represents an nmap-job for the Software Agent.
 * 
 * @author Konstantinos Dalianis
 * @author Maria Fava
 * @version 1.0
 * @since 2015-11-19
 */
public class Job {
	/**
	 * ID of the job.
	 */
	private int id;
	/**
	 * Parameters of the job (nmap parameters).
	 */
	private String parameters;
	/**
	 * If the job is periodic then it repeats itself after waiting some time.
	 */
	private boolean periodic;
	/**
	 * If the job is periodic time represents how much the job has to wait
	 * before running again.
	 */
	private int time;

	/**
	 * Constructor of a Job.
	 * 
	 * @param id
	 *            id of the job
	 * @param parameters
	 *            parameters for nmap
	 * @param periodic
	 *            if set to True, job repetas
	 * @param time
	 *            job repeats after specified time
	 */
	public Job(int id, String parameters, boolean periodic, int time) {
		this.id = id;
		this.parameters = parameters;
		this.periodic = periodic;
		this.time = time;
	}

	/**
	 * Getter function for id.
	 * 
	 * @return Job:id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Getter function for parameters.
	 * 
	 * @return Job:parameters
	 */
	public String getParameters() {
		return parameters;
	}

	/**
	 * Getter function for periodic.
	 * 
	 * @return Job:periodic
	 */
	public boolean isPeriodic() {
		return periodic;
	}

	/**
	 * Getter function for time.
	 * 
	 * @return Job:time
	 */
	public int getTime() {
		return time;
	}

	/**
	 * Get all variables in printable format.
	 * 
	 * @return all Job:variables in string format
	 */
	@Override
	public String toString() {
		return "Job [id=" + id + ", parameters=" + parameters + ", periodic=" + periodic + ", time=" + time + "]";
	}
}
