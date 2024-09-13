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

class Scheduler {
    private List<Process> processes;
    private int currentTime;
    private Process runningProcess;
    private Process nextProcess;
    private Process lastProcess;

    public Scheduler(List<Process> processes) {
        this.processes = processes;
        this.currentTime = 0;
        this.runningProcess = null;
        this.nextProcess = null;
    }

    public void run() {
        while (!allProcessesCompleted()) {
            printTimeline();
            updateProcessStates();
            if (processes.stream().allMatch(p -> p.credits <= 0))
                redistributeCredits();
            if(runningProcess == null) 
            {
                Process nextProcess = getNextProcess();
            } 
            else if (runningProcess.credits == 0 || runningProcess.state == ProcessState.BLOCKED) {
                Process nextProcess = getNextProcess();
            }
            if (nextProcess != null)
                executeProcess(nextProcess);
            currentTime++;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean allProcessesCompleted() {
        return processes.stream().allMatch(p -> p.state == ProcessState.EXIT);
    }

    private Process getNextProcess() {
        for (Process process : processes) {
            if (process.state != ProcessState.BLOCKED && process.state != ProcessState.EXIT) {
                if (nextProcess == null)
                    nextProcess = processes.get(3);
                if (process.credits > nextProcess.credits && process != lastProcess)
                        nextProcess = process;
                else if (process.credits == nextProcess.credits && process.order < nextProcess.order && process != lastProcess)
                    nextProcess = process;
            }
        }
        lastProcess = runningProcess;
        return nextProcess;
    }

    private void executeProcess(Process process) {
        if (runningProcess != process) {
            if (runningProcess != null) {
                runningProcess.state = ProcessState.READY;
            }
            runningProcess = process;
            process.state = ProcessState.RUNNING;
            if (process.startTime == 0) {
                process.startTime = currentTime;
            }
            if (process.cpuBurst > 0 && process.execPoint == process.cpuBurst) {
                process.state = ProcessState.BLOCKED;
                process.blockedTimeRemaining = process.ioDuration;
                lastProcess = runningProcess;
                runningProcess = null;
                nextProcess = null;
            }  
        }

        if (process.cpuBurst > 0 && process.execPoint == process.cpuBurst) {
            process.state = ProcessState.BLOCKED;
            process.blockedTimeRemaining = process.ioDuration;
            lastProcess = runningProcess;
            runningProcess = null;
            nextProcess = null;
        }
        if (process.state != ProcessState.BLOCKED && process.state != ProcessState.EXIT) {
            process.remainingCPUTime--;
            process.credits--;
            process.execPoint++;
        }
        if (process.credits == 0 && process.execPoint == process.cpuBurst) {
            process.inBurst = true;
        }
        if (process.remainingCPUTime == 0) {
            process.state = ProcessState.EXIT;
            lastProcess = runningProcess;
            runningProcess = null;
            nextProcess = null;
            process.completionTime = currentTime;
            process.inBurst = false;

        } 
    }

    private void printTimeline() {
        StringBuilder lastLine = new StringBuilder();
        
        for (int i = 0; i <= currentTime; i++) {
            final int currentTimeSlot = i;
            lastLine.setLength(0);
    
            lastLine.append(String.format("%d: ", currentTimeSlot));
            
            processes.forEach(p -> {
                if (p.startTime <= currentTimeSlot && (p.completionTime == 0 || p.completionTime > currentTimeSlot)) {
                    lastLine.append(String.format("%s(%s - credit = %s) ", p.name, p.state, p.credits));
                }
            });
        }
        System.out.println(lastLine.toString());
    }

    private void redistributeCredits() {
        currentTime++;
        for (Process p : processes) {
            if (p.state != ProcessState.EXIT) {
                p.credits = (p.credits / 2) + p.priority;
            }
        }
        lastProcess = runningProcess;
        runningProcess = null;
        nextProcess = null;
        printTimeline();
    }

    private void updateProcessStates() {
        for (Process p : processes) {
            if (p.state == ProcessState.BLOCKED) {
                if (--p.blockedTimeRemaining == -1) {
                    p.state = ProcessState.READY;
                    p.execPoint = 0;
                }
            }
            if (p.inBurst == true && p.state != ProcessState.EXIT) {
                currentTime++;
                p.state = ProcessState.BLOCKED;
                p.blockedTimeRemaining = p.ioDuration;
                lastProcess = runningProcess;
                runningProcess = null;
                nextProcess = null;
                p.inBurst = false;
            }
        }
        if (processes.stream().allMatch(p -> p.state == ProcessState.EXIT)) {
            System.exit(0);
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
