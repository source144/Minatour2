
// Gonen Matias
// CAP 4520
// Parallel & Distributed Processing
// Spring 2022
import java.util.concurrent.atomic.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MinatourPresents {
  // We'll use the concurrent Linked List (LL) according to the book
  // Minatour has 4 servants (threads) that alternate tasks
  //
  // The tasks are
  // 1) Add guest present to LL (by id-tag)
  // 2) Remove present from LL (write "Thank You")
  // 3) Randomly check if present is in LL (by id-tag)
  //
  // The implementation |
  // --------------------'
  // We will have a Concurrent Deque (stack) that will represent
  // our "unordered bag", which is essentially the range from [1, n]
  // presents, where [1, n] is the identifying tag and n is the
  // specified number of presents (default 500,000) and can be set
  // with the --numPresents argument.
  //
  // Task (1) will take a present from the "unordered bag" (the stack)
  // and add it to our implemented Linked List chain in the correct
  // position.
  //
  // Task (2) will remove a present (write "Thank You") from the chain
  //
  // Task (3) will search for a random present (by tag) in the chain.
  // I might implement an IO operation for this task and ask the user
  // for a specific tag to look for, otherwise it will be random number.
  // If I implement it, it would be an optional argument --io.
  //
  //////////////////////////////////////////////////////////////////////

  
  private static final int NUM_SERVANTS = 4;                                          // Number of threads actually
  private static final int MIN_NUM_PRESENTS = 100;                                    // Ensure a lower bound for n
  private static final String DEFAULT_NUM_PRESENTS_ARG = "500000";                    // Default #-of presents (n)
  private static int NUM_PRESENTS = Integer.parseInt(DEFAULT_NUM_PRESENTS_ARG);       // The actual #-of presents
  public static ConcurrentLinkedDeque<Integer> bag = new ConcurrentLinkedDeque<>();   // The unordered bag
  public static ConcurrentLinkedDeque<Integer> chain = new ConcurrentLinkedDeque<>(); // The linked List chain

  // Task Operations (Ops)
  public enum TASK {
    ADD, REMOVE, FIND;

    private static final TASK[] VALUES = values();
    private static final int SIZE = VALUES.length;

    // Get random Operation
    public static TASK RANDOM() {
      return values()[ThreadLocalRandom.current().nextInt(0, SIZE)];
    }
  }

  // For command-line arguments
  private static HashMap<String, String> options = new HashMap<>() {
    {
      put("-numPresents", DEFAULT_NUM_PRESENTS_ARG);
    }
  };
  private static HashSet<String> intArgs = new HashSet<>() {
    {
      add("-numPresents");
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
    NUM_PRESENTS = Integer.max(MIN_NUM_PRESENTS, Integer.parseInt(options.get("-numPresents")));
  }

  public static void main(String[] args) throws Exception {

    // Configuration
    parseArgs(args);

    // Initialize all present tags (in the range of [1, n])
    // These are the members of the "unordered bag"
    List<Integer> tagRange = IntStream.rangeClosed(1, NUM_PRESENTS).boxed().collect(Collectors.toList());

    // Shuffle the bag
    Collections.shuffle(tagRange);

    // Actaully create the "unordered bag"
    // which is the concurrent deque (stack)
    // (using the randomized range above)
    bag.addAll(tagRange);

    // Initialzie the Concurrent Linked List chain
    List<Integer> chain = new ArrayList<>();

    // Initialize the Servant Threads!:)x
    // Linear O(n), where n is NUM_SERVANTS
    Thread[] servants = new Thread[NUM_SERVANTS];
    for (int i = 0; i < NUM_SERVANTS; i++) {
      servants[i] = new Thread(new PresentRun(chain, i));
      servants[i].start(); // Get i-th servant to work
                           // He/They ain't getting paid
                           // to just sit there! wasted RAM.
    }
  }

  // Thread worker
  static class PresentRun implements Runnable {

    private final int servantId;
    public AtomicReference<List<Integer>> chain;

    public PresentRun(List<Integer> chain, int id) {
      this.chain = new AtomicReference<>(chain);
      this.servantId = id;
    }

    public void run() {
      // Get random task
      TASK task = TASK.RANDOM();
      int tag = 0;

      switch (task) {
        case ADD:
          chain.get().add(bag.pop());
          break;
        case REMOVE:
          chain.get().remove(tag);
          break;
        case FIND:
          tag = ThreadLocalRandom.current().nextInt(0, NUM_PRESENTS) + 1;
          chain.get().contains(tag);
          break;
      }
    }
  }

  // Node
  public class Node<T> {
    T item;
    int key;
    Node<T> next;
  }
}

