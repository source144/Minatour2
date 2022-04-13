import java.util.concurrent.atomic.*;

// Based off the book
class LockFreeList {
  public static final int FAIL_FLAG = Integer.MAX_VALUE;
  Node head;

  public LockFreeList() {
    this.head = new Node(Integer.MIN_VALUE);
    Node tail = new Node(Integer.MAX_VALUE);
    while (!head.next.compareAndSet(null, tail, false, false))
      ;
  }

  public boolean isEmpty() {

    // We only need the first window or first occurence
    // So no need to find a specific window

    boolean[] marked = { false };
    Node curr;

    // Start from head->next
    curr = head.next.get(marked);

    // While marked and tail key (safety) -> move to next
    while (marked[0] && curr.key != Integer.MAX_VALUE)
      curr = curr.next.get(marked); // Move to next

    // The list is empty if the cursor stops on the tail
    return curr.key == Integer.MAX_VALUE;
  }

  public int pop() {

    // We only need the first window or first occurence
    // So no need to find a specific window...
    // But strategy here is to find first key that is not marked
    // and remove it. If we fail with the remove we try again
    // We halt if we reach the tail.
    //
    // Note; calling remove will handle preserving links for us

    boolean[] marked = { false };
    boolean snip = true;
    Node curr;
    int key;

    do {
      // Start from head->next
      curr = head.next.get(marked);

      // While marked and tail key (safety) -> move to next
      while (marked[0] && curr.key != Integer.MAX_VALUE)
        curr = curr.next.get(marked); // Move to next

      // The element to remove
      key = curr.key;

      // The list is empty if the cursor stops on the tail
      // so there will be nothing to pop.. return false
      if (key == Integer.MAX_VALUE)
        return Integer.MAX_VALUE; // (Empty list)
      
      snip = remove(key);
    } while (!snip);

    // Successful removal, return element that was removed
    return key;
  }

  public boolean add(int key) {
    while (true) {
      Window window = find(head, key);
      Node pred = window.pred, curr = window.curr;

      if (curr.key != key) {
        Node node = new Node(key);
        node.next = new AtomicMarkableReference<Node>(curr, false);

        if (pred.next.compareAndSet(curr, node, false, false)) {
          return true;
        }
      } else
        return false;
    }
  }

  public boolean remove(int key) {
    boolean snip;

    while (true) {
      Window window = find(head, key);
      Node pred = window.pred, curr = window.curr;

      if (curr.key == key) {
        Node succ = curr.next.getReference();
        snip = curr.next.attemptMark(succ, true);

        if (!snip)
          continue;

        pred.next.compareAndSet(curr, succ, false, false);

        return true;
      } else
        return false;
    }
  }

  public boolean contains(int key) {
    Window window = find(head, key);
    Node curr = window.curr;
    return curr.key == key;
  }

  class Node {
    AtomicMarkableReference<Node> next;
    int key;

    Node(int key) {
      this.key = key;
      this.next = new AtomicMarkableReference<Node>(null, false);
    }
  }

  class Window {
    public Node pred;
    public Node curr;

    Window(Node pred, Node curr) {
      this.pred = pred;
      this.curr = curr;
    }
  }

  public Window find(Node head, int key) {
    Node pred = null, curr = null, succ = null;
    boolean[] marked = { false };
    boolean snip;

    retry: while (true) {
      pred = head;
      curr = pred.next.getReference();

      while (true) {
        succ = curr.next.get(marked);

        while (marked[0]) {
          snip = pred.next.compareAndSet(curr, succ, false, false);

          if (!snip)
            continue retry;

          curr = pred.next.getReference();
          succ = curr.next.get(marked);
        }

        if (curr.key >= key)
          return new Window(pred, curr);

        pred = curr;
        curr = succ;
      }
    }
  }
}