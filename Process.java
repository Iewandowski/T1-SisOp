public class Process {
  String name;
  int cpuBurst;
  int ioDuration;
  int totalCPUTime;
  int order;
  int priority;
  int remainingCPUTime;
  int credits;
  ProcessState state;
  int startTime;
  int completionTime;
  int blockedTimeRemaining;
  int execPoint;
  boolean inBurst;

  public Process(String name, int cpuBurst, int ioDuration, int totalCPUTime, int order, int priority) {
    this.name = name;
    this.cpuBurst = cpuBurst;
    this.ioDuration = ioDuration;
    this.totalCPUTime = totalCPUTime;
    this.order = order;
    this.priority = priority;
    this.remainingCPUTime = totalCPUTime;
    this.credits = priority;
    this.state = ProcessState.READY;
    this.startTime = -1;
    this.completionTime = 0;
    this.execPoint = 0;
    this.inBurst = false;
  }
}