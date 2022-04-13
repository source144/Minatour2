
// Gonen Matias
// CAP 4520
// Parallel & Distributed Processing
// Spring 2022
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MarsRover {
  //
  // We will use two runnable thread types:
  // 1) Sensor Thread
  // 2) Report Thread
  //
  // The sensor threads store data into
  // shared memory on a set interval.
  //
  // The Report thread compile that data
  // every predifined interval of time.
  //
  // The implementation of the shared data
  // is simply an matrix where each row
  // is for each sensor, and each column
  // is for a time entry of the sensor.
  //
  //////////////////////////////////////////////////////////////////////

  // Default/boundary values
  public static final int NUM_SENSORS = 8;
  // public static final long MIN_SENSOR_TIME = 60000;
  // public static final long MIN_CHANGE_TIME = 600000;
  // public static final long MIN_REPORT_TIME = 3600000;
  public static final long MIN_SENSOR_TIME = 500;
  public static final long MIN_CHANGE_TIME = 5000;
  public static final long MIN_REPORT_TIME = 15000;
  public static final int MIN_TEMP = -100;
  public static final int MAX_TEMP = 70;

  // Debug
  private static final String DEFAULT_DEBUG_ARG = "false";
  public static boolean DEBUG = Boolean.parseBoolean(DEFAULT_DEBUG_ARG);

  // Report interval
  private static final String DEFAULT_REPORT_TIME_ARG = String.valueOf(MIN_REPORT_TIME);
  public static long REPORT_TIME = Long.parseLong(DEFAULT_REPORT_TIME_ARG);

  // Sensor interval
  private static final String DEFAULT_SENSOR_TIME_ARG = String.valueOf(MIN_SENSOR_TIME);
  public static long SENSOR_TIME = Long.parseLong(DEFAULT_SENSOR_TIME_ARG);

  // Change interval
  private static final String DEFAULT_CHANGE_TIME_ARG = String.valueOf(MIN_CHANGE_TIME);
  public static long CHANGE_TIME = Long.parseLong(DEFAULT_CHANGE_TIME_ARG);

  // Ordered and concurrent
  // public static ConcurrentLinkedDeque<Integer> temps = new ConcurrentLinkedDeque<>();   // store two report cycles at most
  // public static ConcurrentLinkedDeque<Integer> changes = new ConcurrentLinkedDeque<>(); // 
  public static int[][] temps;

  // For command-line arguments
  private static HashMap<String, String> options = new HashMap<>() {
    {
      put("-sensor", DEFAULT_SENSOR_TIME_ARG);
      put("-report", DEFAULT_REPORT_TIME_ARG);
      put("-change", DEFAULT_CHANGE_TIME_ARG);
      put("--debug", DEFAULT_DEBUG_ARG);
    }
  };
  private static HashSet<String> intArgs = new HashSet<>() {
    {
      add("-sensor");
      add("-report");
      add("-change");
    }
  };
  private static HashSet<String> boolArgs = new HashSet<>() {
    {
      add("--debug");
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
          if (boolArgs.contains(arg))
            options.put(arg, "true");
          // TODO if I want more type of args
        }
      }
    }

    // Apply configuration
    REPORT_TIME = Long.max(MIN_REPORT_TIME, Long.parseLong(options.get("-report")));
    SENSOR_TIME = Long.max(MIN_SENSOR_TIME, Long.parseLong(options.get("-sensor")));
    CHANGE_TIME = Long.max(MIN_CHANGE_TIME, Long.parseLong(options.get("-change")));
    DEBUG = Boolean.parseBoolean(options.get("--debug"));

    if (REPORT_TIME <= SENSOR_TIME || REPORT_TIME <= CHANGE_TIME) {
      System.out.println("Invalid configuration");
      System.out.println("REPORT_TIME=" + REPORT_TIME);
      System.out.println("SENSOR_TIME=" + SENSOR_TIME);
      System.out.println("CHANGE_TIME=" + CHANGE_TIME);
      System.out.println("...");
      System.out.println("Report Time Interval must be greater than Sensor Time and Change Time intervals!");
    }

    if (CHANGE_TIME <= SENSOR_TIME) {
      System.out.println("Invalid configuration");
      System.out.println("REPORT_TIME=" + REPORT_TIME);
      System.out.println("SENSOR_TIME=" + SENSOR_TIME);
      System.out.println("CHANGE_TIME=" + CHANGE_TIME);
      System.out.println("...");
      System.out.println("Sensor Time Interval must be greater than Change Time interval!");
    }

    // Ensure Safety:
    REPORT_TIME = SENSOR_TIME * Math.round((1.0 * REPORT_TIME)/ SENSOR_TIME);
    CHANGE_TIME = SENSOR_TIME * Math.round((1.0 * CHANGE_TIME)/ SENSOR_TIME);
  }

  public static void main(String[] args) throws Exception {

    // Configuration
    parseArgs(args);

    // Print Configuration
    System.out.println("MarsRover.java");
    System.out.println();
    System.out.println("Available arguments:");
    System.out.println("   -sensor {int}  -   the sensor interval in ms      (default: 500)");
    System.out.println("   -report {int}  -   the report interval in ms      (default: 15,000)");
    System.out.println("   -change {int}  -   the change interval in ms      (default: 5,000)");
    System.out.println("   --debug        -   [NOT FUNCTIONAL - NO DEBUGS]   (default: off)");
    System.out.println();
    System.out.println("Current Configuration:");
    System.out.println("REPORT_TIME=" + REPORT_TIME);
    System.out.println("SENSOR_TIME=" + SENSOR_TIME);
    System.out.println("CHANGE_TIME=" + CHANGE_TIME);
    System.out.println("Running with " + NUM_SENSORS + " sensors.");
    System.out.println("\n");

    // Initialize the shared temps matrix
    temps = new int[NUM_SENSORS][(int)(REPORT_TIME / SENSOR_TIME)];

    // Initialize and Start sensors
    Thread[] sensors = new Thread[NUM_SENSORS];
    for (int i = 0; i < NUM_SENSORS; i++) {
      sensors[i] = new Thread(new SensorRun(temps[i], i));
      sensors[i].start(); // Get i-th sensor to work
    }

    // Initialize and Start reporting thread
    Thread report = new Thread(new ReportRun(temps));
    report.start();

    // Press Enter to quit
    System.in.read();
    System.out.println();
    System.out.println("Goodbye! \\(@^0^@)/");
    System.exit(0);

  }
}
