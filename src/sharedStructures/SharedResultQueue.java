package sharedStructures;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import nmapJob.Result;

/**
 * The SharedResultQueue class implements a synchronized Queue. The queue is
 * filled with results from Jobs.
 * 
 * @author Konstantinos Dalianis
 * @author Maria Fava
 * @version 2.0
 * @since 2015-11-19
 */
public class SharedResultQueue {
	/**
	 * Queue of results.
	 */
	private Queue<Result> queue;

	/**
	 * Boolean variable set to true if queue is empty.
	 */
	private boolean empty;

	/**
	 * Constructor of SharedResultQueue.
	 */
	public SharedResultQueue() {
		queue = new LinkedList<Result>();
		empty = true;
	}

	/**
	 * Synchronized getter. Returns the first result found in the queue. Waits
	 * if the queue is empty.
	 * 
	 * @return The first result found in the queue.
	 * @throws InterruptedException
	 */
	public synchronized Result get() throws InterruptedException {
		if (empty == true) {
			wait();
		}
		Result result = queue.poll();
		if (queue.size() == 0) {
			empty = true;
		}
		return result;
	}

	/**
	 * Synchronized insertion to the queue.
	 * 
	 * @param result
	 *            Result to be inserted in the end of the queue.
	 */
	public synchronized void put(Result result) {
		queue.add(result);
		empty = false;
		notify();
	}

	/**
	 * Synchronized function to get all results from the queue.
	 * 
	 * @throws InterruptedException
	 */
	public synchronized List<Result> getAllResults() throws InterruptedException {
		if (empty == true) {
			wait();
		}
		List<Result> resultList = new ArrayList<Result>(queue);
		queue.clear();
		empty = true;
		return resultList;
	}

}
