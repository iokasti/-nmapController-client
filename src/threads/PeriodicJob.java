package threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import nmapJob.Job;
import nmapJob.Result;
import softwareAgent.Main;

/**
 * The PeriodicJob class represents a PeriodicJob thread.
 * 
 * @author Konstantinos Dalianis
 * @author Maria Fava
 * @version 2.0
 * @since 2015-11-19
 */
public class PeriodicJob implements Runnable {
	/**
	 * The job the PeriodicJob thread is running.
	 */
	private Job job;

	/**
	 * A static list used to save all the execed Processes (nmap) so they can
	 * normally exit in case of program termination.
	 */
	public static ArrayList<Process> runningPeriodicJobExecs = new ArrayList<Process>();

	/**
	 * Constructor of PeriodicJob.
	 * 
	 * @param job
	 *            Job to be executed
	 * @param results
	 *            SharedResultQueue used to put Results
	 */
	public PeriodicJob(Job job) {
		this.job = job;
	}

	/**
	 * PeriodicJob thread function. Execs nmap for the given Job in defined time
	 * intervals. Reads the result of nmap to a string and put the Result in the
	 * SharedResultQueue.
	 */
	public void run() {
		System.err.println("Started Periodic Job Thread: " + Thread.currentThread().getId());
		while (true) {
			try {
				// create command and run nmap
				String command = "nmap -oX - " + job.getParameters();
				Process p = Runtime.getRuntime().exec(command);
				BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

				synchronized (runningPeriodicJobExecs) {
					runningPeriodicJobExecs.add(p);
				}

				// save results of nmap in string
				String result = "";
				String line;
				if (Thread.currentThread().isInterrupted() == true) {
					// end
					input.close();
					System.err.println("Terminated Periodic Job Thread: " + Thread.currentThread().getId());
					return;
				}
				while ((line = input.readLine()) != null) {
					if (Thread.currentThread().isInterrupted() == true) {
						// end
						input.close();
						System.err.println("Terminated Periodic Job Thread: " + Thread.currentThread().getId());
						return;
					}
					result += line;
					result += "\n";
				}
				input.close();
				if (Thread.currentThread().isInterrupted() == true) {
					// end
					System.err.println("Terminated Periodic Job Thread: " + Thread.currentThread().getId());
					return;
				}
				// add result string to shared results
				Main.results.put(new Result(result, job));
				Thread.sleep(job.getTime() * 1000);
			} catch (InterruptedException e) {
				// end
				System.err.println("Terminated Periodic Job Thread: " + Thread.currentThread().getId());
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
