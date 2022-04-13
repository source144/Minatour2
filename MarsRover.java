
// Gonen Matias
// CAP 4520
// Parallel & Distributed Processing
// Spring 2022
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MinatourPresents {
	// may also enter the queue.

	public static ConcurrentLinkedQueue<Thread> q = new ConcurrentLinkedQueue<>();
	private static int NUM_GUESTS = 8; // The threads silly

	// For command-line arguments
	private static HashMap<String, String> options = new HashMap<>() {
		{
			put("-threads", "8");
		}
	};
	private static HashSet<String> intArgs = new HashSet<>() {
		{
			add("-threads");
		}
	};

	private static void parseArgs(String[] args) {
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			// Not really complicating things here.. can use libraries for this
			// Just parse number of threads
			if (arg.charAt(0) == '-' && arg.length() >= 2 && options.containsKey(arg)) {
				if (intArgs.contains(arg)) {
					// Parse numeric arg
					if (i + 1 < args.length && args[i + 1].matches("\\d+")) {
						options.put(arg, args[i + 1]);
						i++;
					}
				} else {
					// TODO if I want more type of args
				}
			}
		}

		// Apply configuration
		NUM_GUESTS = Integer.max(2, Integer.parseInt(options.get("-threads")));
	}

	// Function to let Minatour know that all
	// Test Subjects have entered the labyrinth
	public static void joinQueue(Runnable guest) {
		q.add(new Thread(guest));
	}

	public static void main(String[] args) throws Exception {
		int num_visits = 0;
		List<Thread> threads;
		Thread thread;

		// Configuration
		parseArgs(args);

		// Generate a Queue
		threads = new ArrayList<>();
		for (int i = 0; i < NUM_GUESTS; i++) {
			threads.add(new Thread(new ShowroomRun(i)));
		}
		Collections.shuffle(threads);

		// Add all threads to queue
		q.addAll(threads);

		// Enter the showroom one at a time
		while (!q.isEmpty()) {
			// Increment visits
			num_visits++;

			// Get next in line
			thread = q.poll();

			// Enter Show Room
			if (thread.isAlive())
				thread.start();
			else
				thread.run();

			// Wait until leaves
			thread.join();
		}

		// Conclude the Show Room exhibition
		System.out
				.println("\nAll " + NUM_GUESTS + " guests have now visited the Show Room (total of " + num_visits + " visits)");

		// Minatour was happy everyone showed up:)
		System.out.println("Minatour is very happy");
	}

	// Thread worker
	static class ShowroomRun implements Runnable {

		private int id;

		// Constructor
		public ShowroomRun(int id) {
			this.id = id;
		}

		// Visiting the labyrinth
		public void run() {
			// Test Subject now visiting the labyrinth
			System.out.println("Guest (" + id + ") - now visiting the Show Room");

			// Decide to whether he wants to visit again
			if (ThreadLocalRandom.current().nextBoolean()) {
				System.out.println("Guest (" + id + ") - decided rejoin the queue and visit the Show Room again");
				MinatourShowroom.joinQueue(this);
			} else
				System.out.println("Test Subject (" + id + ") - decided they've seen enough of the Show Room");
		}
	}
}