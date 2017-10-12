package sharedStructures;

import java.util.LinkedList;
import java.util.Queue;

import nmapJob.Job;

/**
 * The SharedJobQueue class implements a synchronized Queue. The queue is filled
 * with one Time Jobs. Workers from the oneTimeJob thread pool get their jobs
 * from this queue.
 * 
 * @author Konstantinos Dalianis
 * @author Maria Fava
 * @version 1.0
 * @since 2015-11-19
 */
public class SharedOneTimeJobQueue {
	/**
	 * Queue of jobs.
	 */
	private Queue<Job> queue;

	/**
	 * Boolean variable set to true if queue is empty.
	 */
	private boolean empty;

	/**
	 * Constructor of SharedJobQueue.
	 */
	public SharedOneTimeJobQueue() {
		queue = new LinkedList<Job>();
		empty = true;
	}

	/**
	 * Synchronized getter. Returns the first Job found in the queue. Waits if
	 * the queue is empty.
	 * 
	 * @return The first job found in the queue.
	 * @throws InterruptedException
	 */
	public synchronized Job get() throws InterruptedException {
		if (empty == true) {
			wait();
		}
		Job job = queue.poll();
		if (queue.size() == 0) {
			empty = true;
		}
		return job;
	}

	/**
	 * Synchronized insertion to the queue.
	 * 
	 * @param job
	 *            Job to be inserted in the end of the queue.
	 */
	public synchronized void put(Job job) {
		queue.add(job);
		empty = false;
		notify();
	}

}
