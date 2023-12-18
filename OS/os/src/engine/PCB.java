package engine;

import java.io.Serializable;

public class PCB implements Serializable {

    private int processID;
    private ProcessState state;
    private int PC;
    private int start;
    private int end;

    public PCB(int processID, ProcessState state, int PC, int start, int end) {
        this.processID = processID;
        this.state = state;
        this.PC = PC;
        this.start = start;
        this.end = end;
    }


    public int getProcessID() {
        return processID;
    }

    public void setProcessID(int processID) {
        this.processID = processID;
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public int getPC() {
        return PC;
    }

    public void setPC(int PC) {
        this.PC = PC;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

}
