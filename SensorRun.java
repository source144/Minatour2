import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class SensorRun implements Runnable {
  private final int sensorId;
  private final int[] temps;
  private long idx = 0;

  public SensorRun(int[] temps, int id) {
    this.temps = temps;
    this.sensorId = id;
    this.idx = 0;
  }

  // Random temp
  private static int getTemp() {
    return ThreadLocalRandom.current().nextInt(MarsRover.MIN_TEMP, MarsRover.MAX_TEMP + 1);
  }

  public void run() {
    // PERFORM SENSING TASK UNTIL TA IS SATISFIED!!
    do { //                         (╯°□°）╯︵ ┻━┻

      // Insert to correct time slot
      temps[(int) (idx++ % temps.length)] = getTemp();

      // Sleep until next read
      try { Thread.sleep(MarsRover.SENSOR_TIME); }
      catch (InterruptedException e) { }

    } while (true);
  }
}
