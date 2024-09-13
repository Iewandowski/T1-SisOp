import java.util.*;

public class ProcessScheduler {
  public static void main(String[] args) {
    List<Process> processes = Arrays.asList(
            new Process("A", 2, 5, 6, 1, 3),
            new Process("B", 3, 10, 6, 2, 3),
            new Process("C", 0, 0, 14, 3, 3),
            new Process("D", 0, 0, 10, 4, 3)
    );

    new Scheduler(processes).run();
  }
}
