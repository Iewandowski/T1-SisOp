import java.util.*;

enum ProcessState {
    READY,
    RUNNING,
    BLOCKED,
    EXIT
}

class Process {
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
    }
}

class Scheduler {
    private List<Process> processes;
    private int currentTime;
    private Process runningProcess;

    public Scheduler(List<Process> processes) {
        this.processes = processes;
        this.currentTime = 0;
        this.runningProcess = null;
    }

    public void run() {
        while (!allProcessesCompleted()) {
            Process nextProcess = getNextProcess();
            if (nextProcess != null) {
                executeProcess(nextProcess);
            } else {
                redistributeCredits();
            }
            updateProcessStates();
            currentTime++;
        }
        printResults();
    }

    private boolean allProcessesCompleted() {
        return processes.stream().allMatch(p -> p.state == ProcessState.EXIT);
    }

    private Process getNextProcess() {
        return processes.stream()
                .filter(p -> p.state == ProcessState.READY && p.credits > 0)
                .max(Comparator.comparingInt((Process p) -> p.credits)
                        .thenComparingInt(p -> p.order))
                .orElse(null);
    }

    private void executeProcess(Process process) {
        if (runningProcess != process) {
            if (runningProcess != null) {
                runningProcess.state = ProcessState.READY;
            }
            runningProcess = process;
            process.state = ProcessState.RUNNING;
            if (process.startTime == -1) {
                process.startTime = currentTime;
            }
        }

        process.remainingCPUTime--;
        process.credits--;

        if (process.remainingCPUTime == 0) {
            process.state = ProcessState.EXIT;
            process.completionTime = currentTime;
            runningProcess = null;
        } else if (process.cpuBurst > 0 && process.remainingCPUTime % process.cpuBurst == 0) {
            process.state = ProcessState.BLOCKED;
            runningProcess = null;
        }
    }

    private void redistributeCredits() {
        for (Process p : processes) {
            if (p.state != ProcessState.EXIT) {
                p.credits = p.credits / 2 + p.priority;
            }
        }
    }

    private void updateProcessStates() {
        for (Process p : processes) {
            if (p.state == ProcessState.BLOCKED) {
                if (currentTime % p.ioDuration == 0) {
                    p.state = ProcessState.READY;
                }
            }
        }
    }

    private void printResults() {
        System.out.println("Process Scheduling Results:");
        for (Process p : processes) {
            int turnaroundTime = p.completionTime - p.startTime;
            System.out.printf("Process %s: Turnaround Time = %d ms%n", p.name, turnaroundTime);
        }
    }
}

public class ProcessScheduler {
    public static void main(String[] args) {
        List<Process> processes = Arrays.asList(
            new Process("A", 2, 5, 6, 1, 3),
            new Process("B", 3, 10, 6, 2, 3),
            new Process("C", 0, 0, 14, 3, 3),
            new Process("D", 0, 0, 10, 4, 3)
        );
        Scheduler scheduler = new Scheduler(processes);
        scheduler.run();
    }
}
