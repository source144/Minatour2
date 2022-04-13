import java.util.concurrent.ThreadLocalRandom;

// Task Operations (Ops)
public enum Task {
  ADD, REMOVE, FIND;

  private static final Task[] VALUES = values();
  private static final int SIZE = VALUES.length;

  // Get random Operation
  public static Task RANDOM() {
    return values()[ThreadLocalRandom.current().nextInt(0, SIZE)];
  }
}