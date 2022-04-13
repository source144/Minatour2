import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public class ReportRun implements Runnable {
  private final int[][] temps;
  private int idx;

  public ReportRun(int[][] temps) {
    this.temps = temps;
    this.idx = 0;
  }

  private String getTime(int sensorIdx) {
    long t = (MarsRover.REPORT_TIME * idx) + (MarsRover.SENSOR_TIME * sensorIdx);
    long h = TimeUnit.MILLISECONDS.toHours(t) % 24;
    long m = TimeUnit.MILLISECONDS.toMinutes(t) % 60;
    long s = TimeUnit.MILLISECONDS.toSeconds(t) % 60;

    return String.format("%02d", h) + ":" + String.format("%02d", m) + ":" + String.format("%02d", s);
  }

  private void printReport(int[] maxVector, int[] minVector) {

    // Prep arrays for top 5
    int[] maxTempsIdxs, minTempsIdxs;

    // Convert the timings to indecies
    int changePeriod = (int) (MarsRover.CHANGE_TIME / MarsRover.SENSOR_TIME);
    int d1 = 0, d2 = 0;
    int biggestChange = Integer.MIN_VALUE;
    int biggetChangeIdx = -1;

    // Find biggest delta and the time slot of that delta
    for (int i = changePeriod; i < maxVector.length; i++) {
      int _d11 = maxVector[i - changePeriod];
      int _d12 = minVector[i];
      int _d1 = Math.abs(minVector[i] - maxVector[i - changePeriod]);
      int _d21 = minVector[i - changePeriod];
      int _d22 = maxVector[i];
      int _d2 = Math.abs(maxVector[i] - minVector[i - changePeriod]);

      int delta = Integer.max(_d1, _d2);

      if (delta > biggestChange) {
        biggestChange = delta;
        biggetChangeIdx = i;
        if (_d1 > _d2) {
          d1 = _d11;
          d2 = _d12;
        }
        else {
          d1 = _d21;
          d2 = _d22;
        }
      }
    }

    // Get top 5 min and max temps
    maxTempsIdxs = IntStream.range(0, maxVector.length).boxed()
        .sorted((o1, o2) -> maxVector[o2] - maxVector[o1])
        .mapToInt(ele -> ele).limit(5).toArray();

    minTempsIdxs = IntStream.range(0, minVector.length).boxed()
        .sorted((o1, o2) -> minVector[o1] - minVector[o2])
        .mapToInt(ele -> ele).limit(5).toArray();

    String maxTempsStr = "    Top " + maxTempsIdxs.length + " Max Temps\n";
    for (int i = 0; i < maxTempsIdxs.length; i++)
      maxTempsStr += "    (" + (i + 1) + ") " +  String.format("%4d", maxVector[maxTempsIdxs[i]]) + "°F @ " + getTime(maxTempsIdxs[i]) + "  [entry " + String.format("%03d", maxTempsIdxs[i] + 1) + "]\n";
      
    String minTempsStr = "    Top " + maxTempsIdxs.length + " Min Temps\n";
    for (int i = 0; i < minTempsIdxs.length; i++)
      minTempsStr += "    (" + (i + 1) + ") " +  String.format("%4d", minVector[minTempsIdxs[i]]) + "°F @ " + getTime(minTempsIdxs[i]) + "  [entry " + String.format("%03d", minTempsIdxs[i] + 1) + "]\n";

    String changeStr = biggetChangeIdx >= changePeriod ?
        "     The biggest temperature change was     \n"
      + "         " + String.format("%4d", d1) + "°F     »»»      " + String.format("%4d", d2) + "°F           \n"
      + "        " + getTime(biggetChangeIdx - changePeriod) + "     -     " + getTime(biggetChangeIdx) + "         \n"
      + "               (delta=" + String.format("%3d", biggestChange) + "°F)                 \n"
      + ""
      : "";

    // Print report
    System.out.println("                                              ");
    System.out.println("                                              ");
    System.out.println("##############################################");
    System.out.println("                  Report " + String.format("%03d", ++idx));
    System.out.println(" -------------------------------------------- ");
    System.out.println("                                             ");
    System.out.println(maxTempsStr);
    System.out.println("                                             ");
    System.out.println(minTempsStr);
    System.out.println("                                             ");
    System.out.println(changeStr);
    System.out.println("##############################################");
    System.out.println("                                             ");

  }

  // Random temp
  public void run() {

    // PERFORM SENSING TASK UNTIL TA IS SATISFIED!!
    do { // (╯°□°）╯︵ ┻━┻

      // Print quit statement
      String quitStr = "Sensors are working! Press Enter to quit...";
      String blank   = "                                           ";
      System.out.print(quitStr);
      System.out.print("\r");

      // Start by sleeping because we have nothing
      // to report for the first "hour" ...
      try {
        Thread.sleep(MarsRover.REPORT_TIME);
      } catch (InterruptedException e) {
      }

      // Convert matrix to two vectors of minimum and maximum
      int[] minVector, maxVector;
      maxVector = new int[temps[0].length];
      minVector = new int[temps[0].length];

      // Populate the vectors
      for (int i = 0; i < temps[0].length; i++) {
        int minTemp = Integer.MAX_VALUE;
        int maxTemp = Integer.MIN_VALUE;

        for (int j = 0; j < temps.length; j++) {
          minTemp = Integer.min(minTemp, temps[j][i]);
          maxTemp = Integer.max(maxTemp, temps[j][i]);
        }

        minVector[i] = minTemp;
        maxVector[i] = maxTemp;
      }

      // Clear quit prompt
      System.out.print(blank);

      // Parse and Print report according to vectors
      printReport(maxVector, minVector);

    } while (true);

  }
}