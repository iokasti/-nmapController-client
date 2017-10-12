package softwareAgent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import device.DeviceInfo;
import sharedStructures.SharedOneTimeJobQueue;
import sharedStructures.SharedResultQueue;
import threads.OneTimeJob;
import threads.PeriodicJob;
import threads.RequestJobs;
import threads.Sender;

/**
 * Main class.
 * 
 * @author Konstantinos Dalianis
 * @author Maria Fava
 * @version 2.0
 * @since 2015-11-19
 */
public class Main {
	/**
	 * List with oneTimeJob Threads used as thread pool.
	 */
	private static ArrayList<Thread> oneTimeJobThreadPool;

	/**
	 * Maps a job id to a periodic job thread.
	 */
	public static Map<Integer, Thread> periodicJobThreadPool;

	/**
	 * Sender thread.
	 */
	private static Thread senderThread;

	/**
	 * Main thread.
	 */
	private static Thread mainThread;

	/**
	 * Requests jobs thread.
	 */
	private static Thread requestsThread;

	/**
	 * Shared queue for one time jobs.
	 */
	public static SharedOneTimeJobQueue oneTimeJobs;

	/**
	 * Shared queue for results.
	 */
	public static SharedResultQueue results;

	/**
	 * Set true if SA was canceled.
	 */
	public static boolean canceled;

	/**
	 * Function to attach the shutdown hook.
	 */
	public void attachShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					System.err.println("Terminating all threads...");

					if (canceled == false) {
						/** TERMINATE PERIODIC JOB THREADS **/
						synchronized (PeriodicJob.runningPeriodicJobExecs) {
							for (Process p : PeriodicJob.runningPeriodicJobExecs) {
								p.destroyForcibly();
							}
						}
						for (Thread thread : periodicJobThreadPool.values()) {
							thread.interrupt();
							thread.join(1500);
						}
						/********************************/

						/** TERMINATE ONE TIME JOB THREADS **/
						synchronized (OneTimeJob.runningOneTimeJobExecs) {
							for (Process p : OneTimeJob.runningOneTimeJobExecs) {
								p.destroyForcibly();
							}
						}
						for (Thread thread : oneTimeJobThreadPool) {
							thread.interrupt();
							thread.join(1500);
						}
						/****************************/

						/** TERMINATE SENDER THREAD **/
						senderThread.interrupt();
						senderThread.join();
						/****************************/

						/** TERMINATE REQUESTS THREAD **/
						requestsThread.interrupt();
						requestsThread.join();
						/********************************/

						/** TERMINATE MAIN THREAD **/
						mainThread.interrupt();
						mainThread.join();
						/****************************/
					}

					System.err.println("Terminating threads completed. Now exiting.");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Main function. Initialize all public static variables, register the SA to
	 * the AM, create thread pools and start all needed threads.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		/** ATTACH SHUTDOWN HOOK **/
		Main sh = new Main();
		sh.attachShutDownHook();
		mainThread = Thread.currentThread();
		System.err.println("Started Main Thread: " + Thread.currentThread().getId());
		/****************************/

		/** READ PROPERTIES FROM PROPERTY FILE **/
		Properties.setProperties("Properties");
		/****************************/

		/** SHARED STRUCTURES INIT **/
		oneTimeJobs = new SharedOneTimeJobQueue();
		results = new SharedResultQueue();
		DeviceInfo.getDeviceInfo();
		/****************************/

		/** REGISTER AGENT TO SERVER **/
		Register.register();
		/****************************/

		/** ONE TIME JOB THREAD POOL **/
		oneTimeJobThreadPool = new ArrayList<Thread>();
		for (int i = 0; i < Properties.threadPoolSize; i++) {
			OneTimeJob oneTimeJob = new OneTimeJob();
			Thread oneTimeJobThread = new Thread(oneTimeJob);
			oneTimeJobThread.start();
			oneTimeJobThreadPool.add(oneTimeJobThread);
		}
		/*****************************/

		/** PERIODIC JOB THREAD POOL **/
		periodicJobThreadPool = new HashMap<Integer, Thread>();
		/*****************************/

		/** SENDER THREAD **/
		Sender sender = new Sender();
		senderThread = new Thread(sender);
		senderThread.start();
		/*****************************/

		/** THREAD REQUEST JOBS FROM SERVER **/
		RequestJobs requestJobsClient = new RequestJobs();
		requestsThread = new Thread(requestJobsClient);
		requestsThread.start();
		/**************************/
	}
}