import java.util.concurrent.ThreadLocalRandom;

class PresentRun implements Runnable {
  private final int servantId;
  private final int NUM_PRESENTS;
  private LockFreeList<String> chain;

  public PresentRun(LockFreeList<String> chain, int id, int numPresents) {
    this.chain = chain;
    this.servantId = id;
    this.NUM_PRESENTS = numPresents;
  }

  public void run() {
    // Get random task
    Task task = Task.RANDOM();
    int tag = 0;

    switch (task) {
      case ADD:
        // chain.get().add(bag.pop());
        break;
      case REMOVE:
        // chain.get().remove(tag);
        break;
      case FIND:
        tag = ThreadLocalRandom.current().nextInt(0, NUM_PRESENTS) + 1;
        // chain.get().contains(tag);
        break;
    }
  }
}