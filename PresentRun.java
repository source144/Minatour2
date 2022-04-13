import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

class PresentRun implements Runnable {
  private final int servantId;
  private final int NUM_PRESENTS;
  private AtomicReference<LockFreeList> chain;

  public PresentRun(LockFreeList chain, int id, int numPresents) {
    this.chain = new AtomicReference<>(chain);
    this.servantId = id;
    this.NUM_PRESENTS = numPresents;
  }

  public void run() {
    // PERFORM TASKS UNTIL ALL WORK IS DONE SERVANT!!
    // (╯°□°）╯︵ ┻━┻
    while (MinatourPresents.thankYous.get() < NUM_PRESENTS) {
      // Get random task
      boolean opSuccess, isBagEmpty;
      long start, end;
      int tag = 0, numThankYous = MinatourPresents.thankYous.get();
      Task task;

      // Empty states
      isBagEmpty = MinatourPresents.bag.isEmpty();

      // Get random task to perform
      // But deny ADD task if unsorted bag is empty
      for (task = Task.RANDOM(); isBagEmpty && task == Task.ADD; task = Task.RANDOM())
        ;

      switch (task) {
        case ADD:
          // Random present from the bag
          tag = MinatourPresents.bag.pop();

          // Time elapsed
          start = System.currentTimeMillis();

          // Try to add to concurrent chain
          opSuccess = chain.get().add(tag);

          // Time elapsed
          end = System.currentTimeMillis();

          if (MinatourPresents.DEBUG)
            System.out.println(
                opSuccess
                    ? "-SUCCESS- (" + numThankYous + ") Servant " + servantId + " added present #" + tag
                        + " to the concurrent chain!"
                    : "-FAILED!- (" + numThankYous + ") Servant " + servantId + " FAILED to add present #" + tag
                        + "to the concurrent chain!");
          break;

        case REMOVE:
          // Try to remove first present from the concurrent chain
          // TODO : optionally use IO
          tag = chain.get().pop();

          // Time elapsed
          start = System.currentTimeMillis();

          // Whether success or not
          opSuccess = tag != LockFreeList.FAIL_FLAG;

          // Time elapsed
          end = System.currentTimeMillis();

          // Increment number of "Thank You"s written
          if (opSuccess)
            numThankYous = MinatourPresents.thankYous.incrementAndGet();

          if (MinatourPresents.DEBUG)
            System.out.println(
                opSuccess
                    ? "-SUCCESS- (" + numThankYous + ") Servant " + servantId + " removed present #" + tag
                        + " from the concurrent chain!"
                    : "-FAILED!- (" + numThankYous + ") Servant " + servantId
                        + " FAILED to remove present from chain, chain is empty!");
          break;

        case FIND:
          // Select a random tag to look for
          // TODO : optionally use IO
          tag = ThreadLocalRandom.current().nextInt(0, NUM_PRESENTS) + 1;

          // Time elapsed
          start = System.currentTimeMillis();

          // Look for the tag in the concurrent chain
          opSuccess = chain.get().contains(tag);

          // Time elapsed
          end = System.currentTimeMillis();

          if (MinatourPresents.DEBUG)
            System.out.println(
                opSuccess
                    ? "-SUCCESS- (" + numThankYous + ") Servant " + servantId + " found present #" + tag
                        + " in the concurrent chain!"
                    : "-SUCCESS- (" + numThankYous + ") Servant " + servantId + " FAILED to find present #" + tag
                        + " in the concurrent chain!");
          break;
      }
    }
    if (MinatourPresents.DEBUG)
      System.out.println("Servant " + servantId + " is done!");
  }
}