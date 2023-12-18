package engine;

import java.util.ArrayList;

public class Mutex {

    private boolean status;
    ArrayList<Integer> waitingProcesses;

    public Mutex(){
        status = true;
        waitingProcesses = new ArrayList<>();
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public ArrayList<Integer> getWaitingProcesses() {
        return waitingProcesses;
    }

    public boolean semWait(int processId){
        if (status == true){
            System.out.println("Resource accuired!");
            status = false;
            return true;
        }
        else {
            System.out.println("Resource not availabale! Process is blocked");
            waitingProcesses.add(processId);
            return false;
        }
    }

    public ArrayList<Integer> semSignal(){
        System.out.println("Resource released!");
        status = true;
        return waitingProcesses;
    }

    public void clearWaitingProcesses(){
        waitingProcesses.clear();
    }
}
