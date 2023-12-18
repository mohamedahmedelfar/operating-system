package engine;

import java.io.*;
import java.util.*;

public class OperatingSystem {

    private ArrayList<Process> memory;
    private Hashtable<Integer,Integer> processesLocations;
    private Hashtable<Integer,Integer> completedInstructions;
    private Hashtable<Integer,Integer> processBlockSize;
    private Hashtable<Integer,Object> processesInput;
    private ArrayList<Integer> processesOnDisk;

    private int availableMemorySpace;
    private int numberOfProcesses;

    private LinkedList<Integer> readyQueue;
    private LinkedList<Integer> blockedQueue;

    private Mutex fileMutex;
    private Mutex inputMutex;
    private Mutex outputMutex;

    private int maximumInstructionsPerSlice;
    private int cycleNumber;

    public OperatingSystem(){
        memory = new ArrayList<>();
        availableMemorySpace = 40;
        numberOfProcesses = 0;
        readyQueue = new LinkedList<>();
        blockedQueue = new LinkedList<>();
        processesLocations = new Hashtable<Integer,Integer>();
        completedInstructions = new Hashtable<Integer,Integer>();
        processBlockSize = new Hashtable<Integer,Integer>();
        processesInput = new Hashtable<Integer,Object>();
        processesOnDisk = new ArrayList<>();
        maximumInstructionsPerSlice = 2;
        inputMutex = new Mutex();
        outputMutex = new Mutex();
        fileMutex = new Mutex();
        cycleNumber = 0;

    }

    public ArrayList<Process> getMemory() {
        return memory;
    }

    public Hashtable<Integer,Integer> getProcessesLocations() {
        return processesLocations;
    }

    public int getAvailableMemorySpace() {
        return availableMemorySpace;
    }

    public void setAvailableMemorySpace(int availableMemorySpace) {
        this.availableMemorySpace = availableMemorySpace;
    }

    public int getNumberOfProcesses() {
        return numberOfProcesses;
    }

    public void setNumberOfProcesses(int numberOfProcesses) {
        this.numberOfProcesses = numberOfProcesses;
    }

    public LinkedList<Integer> getReadyQueue() {
        return readyQueue;
    }

    public LinkedList<Integer> getBlockedQueue() {
        return blockedQueue;
    }

    public Mutex getFileMutex() {
        return fileMutex;
    }

    public Mutex getInputMutex() {
        return inputMutex;
    }

    public Mutex getOutputMutex() {
        return outputMutex;
    }

    public Hashtable<Integer, Integer> getCompletedInstructions() {
        return completedInstructions;
    }


    public Hashtable<Integer, Integer> getProcessBlockSize() {
        return processBlockSize;
    }

    public Hashtable<Integer, Object> getProcessesInput() {
        return processesInput;
    }

    public int getCycleNumber() {
        return cycleNumber;
    }

    public void incrementClockCycles() {
        this.cycleNumber++;
    }

    public void createProcess(String filePath) throws IOException {
        File file = new File(filePath);
        ArrayList<String> instructions = new ArrayList<>();

        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()){
                String line = scanner.nextLine();
                instructions.add(line);
            }
            scanner.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }


        Integer a = null;
        Integer b = null;
        Integer c = null;

        int oldNumOfProcesses = numberOfProcesses;
        PCB pcb = new PCB(++numberOfProcesses,ProcessState.READY,0,0,instructions.size()-1);
        Process process = new Process(a,b,c,pcb,instructions);
        System.out.println("Process "+process.getPcb().getProcessID()+" arrived");

        int allocatedSpaceInMemory = 8 + instructions.size();
        if(allocatedSpaceInMemory > availableMemorySpace){
            System.out.println("Allocated memory size is not aplicaple ");
            writeToDisk();
            updateProcessesLocations();
        }
        addNewProcess(process);
    }

    public void updateProcessesLocations(){
        processesLocations.clear();
        for(Process process : memory){
            processesLocations.put(process.getPcb().getProcessID(),memory.indexOf(process));
        }
    }
    public void writeToDisk() throws IOException {
        Integer removedProcessId = null;
        if(readyQueue.size() == 1){
            removedProcessId = memory.get(0).getPcb().getProcessID();
        }
        else
            removedProcessId = readyQueue.getLast();
        System.out.println("Removing process "+removedProcessId);
        int processLocation = processesLocations.get(removedProcessId);
        Process removedProcess = memory.remove((int)processLocation);
        updateProcessesLocations();
        availableMemorySpace += 8 + removedProcess.getInstructions().size();
        processesOnDisk.add(removedProcessId);

        System.out.println("Writing process "+removedProcessId+" into disk");
        String filePath = "src/disk/"+removedProcessId+".bin";
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(removedProcess);
        objectOutputStream.close();
        fileOutputStream.close();
    }

    public void addNewProcess(Process process){
        availableMemorySpace -= 8 + process.getInstructions().size();
        memory.add(process);
        updateProcessesLocations();
        completedInstructions.put(process.getPcb().getProcessID(),0);
        processBlockSize.put(process.getPcb().getProcessID(),8+process.getInstructions().size());
        readyQueue.add(process.getPcb().getProcessID());
        System.out.println("Process "+process.getPcb().getProcessID()+" is added to memory");
    }

    public Process swapOutFromDisk(int processId) throws IOException, ClassNotFoundException {
        String filePath = "src/disk/"+processId+".bin";
        FileInputStream fileInputStream = new FileInputStream(filePath);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

        Process process = (Process) objectInputStream.readObject();
        objectInputStream.close();
        fileInputStream.close();
        System.out.println("Getting process "+processId+" from disk");
        processesOnDisk.remove((Object)processId);
        return process;
    }

    public Process chooseProcess() throws IOException, ClassNotFoundException {
        int processId = readyQueue.getFirst();
        if(processesOnDisk.contains(processId)){
            Process process = swapOutFromDisk(processId);
            process.getPcb().setState(ProcessState.RUNNING);


            if(availableMemorySpace < processBlockSize.get(process.getPcb().getProcessID()))
                writeToDisk();

            memory.add(process);
            availableMemorySpace -= 8 + process.getInstructions().size();
            updateProcessesLocations();
            return process;
        }

        int processLocation = processesLocations.get(processId);
        System.out.println("Process "+processId+" is chosen");
        Process process = memory.get(processLocation);
        process.getPcb().setState(ProcessState.RUNNING);
        return process;
    }

    public void reSchedule(boolean doubledInstruction){
        int processId = readyQueue.getFirst();
        int processLocation = processesLocations.get(processId);
        Process currentProcess = memory.get(processLocation);

        int oldInstructions = completedInstructions.get(processId);
        completedInstructions.put(processId,oldInstructions+1);

        currentProcess.getPcb().setState(ProcessState.RUNNING);
        if(!doubledInstruction){
            currentProcess.getPcb().setPC(currentProcess.getPcb().getPC()+1);
        }
        if(currentProcess.getPcb().getPC() >= currentProcess.getInstructions().size()){
            currentProcess.getPcb().setState(ProcessState.COMPLETED);
            memory.remove((int)processLocation);
            availableMemorySpace += 8 + currentProcess.getInstructions().size();
            updateProcessesLocations();
            readyQueue.remove(0);
        }

        if(completedInstructions.get(processId) >= maximumInstructionsPerSlice){
            currentProcess.getPcb().setState(ProcessState.READY);
            if(readyQueue.isEmpty())
                return;
            readyQueue.add(readyQueue.remove());
            completedInstructions.put(processId,0);
        }

    }

    public Object takeUserInput(){
        Scanner scanner = new Scanner(System.in);
        Object result = "";
        if(scanner.hasNextInt()){
            result = scanner.nextInt();
        }
        else if(scanner.hasNextDouble()){
            result = scanner.nextDouble();
        }
        else {
            result = scanner.nextLine();
        }

        reSchedule(true);
        return result;

    }

    public void writeToMemory(String var, int processID,Object value){
        int processLocation = processesLocations.get(processID);
        Process process = memory.get(processLocation);

        switch (var){
            case "a" : process.setA(value);System.out.println("Process "+processID+" is setting a = "+process.getA());break;
            case "b" : process.setB(value);System.out.println("Process "+processID+" is setting b = "+process.getB());break;
            case "c" : process.setC(value);System.out.println("Process "+processID+" is setting c = "+process.getC());break;
        }

        reSchedule(false);
    }


    public Object readFile(String var , int processId){

        int processLocation = processesLocations.get(processId);
        Process process = memory.get(processLocation);
        String filePath = "src/disk/";

        switch (var){
            case "a" : filePath += process.getA();break;
            case "b" : filePath += process.getB();break;
            case "c" : filePath += process.getC();break;
        }

        File file = new File(filePath);
        String str = "";
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()){
                str += scanner.nextLine();
            }
            scanner.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        reSchedule(true);
        System.out.println(str);
        return str;
    }
    public void print(String var, int processId) {
        int processLocation = processesLocations.get(processId);
        Process process = memory.get(processLocation);
        switch (var){
            case "a":System.out.println("Process "+processId+" prints a = "+process.getA());break;
            case "b":System.out.println("Process "+processId+" prints b = "+process.getB());break;
            case "c":System.out.println("Process "+processId+" prints c = "+process.getC());break;
        }
        reSchedule(false);
    }

    public void printFromTo(String x, String y, int processId) {
        int processLocation = processesLocations.get(processId);
        Process process = memory.get(processLocation);
        int a = 0,b = 0;
        switch (x) {
            case "a": a = (int)process.getA();break;
            case "b": a = (int)process.getB();break;
            case "c": a = (int)process.getC();break;
        }
        switch (y) {
            case "a": b = (int)process.getA();break;
            case "b": b = (int)process.getB();break;
            case "c": b = (int)process.getC();break;
        }
        System.out.println("Process "+processId+" is printing numbers FROM "+a+" TO "+b);
        for (int i=a;i<=b;i++) {
                System.out.print(i + " , ");
        }
        reSchedule(false);
    }

    public void writeFile(String x, String y, int processId) throws IOException {
        int processLocation = processesLocations.get(processId);
        Process process = memory.get(processLocation);
        String filename = "src/disk/";
        Object data = null;
        switch (x) {
            case "a":  filename += (String)process.getA();break;
            case "b":  filename += (String)process.getB();break;
            case "c":  filename += (String)process.getC();break;
        }
        switch (y) {
            case "a": data = (Object)process.getA();break;
            case "b": data = (Object)process.getB();break;
            case "c": data = (Object)process.getC();break;
        }
        System.out.println("Process "+processId+" is writing "+data+" to file "+filename);
        FileWriter fileWriter = new FileWriter(filename);
        fileWriter.write(data.toString());
        fileWriter.close();

        reSchedule(false);
    }



    public boolean checkAllProcessesFinished(){
        return readyQueue.isEmpty();
    }

    public void semWait(int processId,String mutexName){
        boolean mutexResult = false;
        switch (mutexName){
            case "userInput" : mutexResult = inputMutex.semWait(processId);break;
            case "userOutput" : mutexResult = outputMutex.semWait(processId);break;
            case "file" : mutexResult = fileMutex.semWait(processId);break;
        }

        if (mutexResult){
            reSchedule(false);
        }
        else {
            blockedQueue.add(processId);
            readyQueue.removeFirst();
            completedInstructions.put(processId,0);
        }
    }

    public void semSignal(int processId, String mutexName){

        ArrayList<Integer> waitingProcesses = null;
        Mutex mutex = null;
        switch (mutexName){
            case "userInput" : mutex = inputMutex ;break;
            case "userOutput" : mutex = outputMutex;break;
            case "file" : mutex = fileMutex;break;
        }

        waitingProcesses = mutex.semSignal();
        for (Integer pid : waitingProcesses){
            blockedQueue.remove(pid);
            readyQueue.add(pid);
        }
        mutex.clearWaitingProcesses();
        reSchedule(false);
    }

    public void displayMemoryContent(){
        int memoryLocation = -1;
        for (Process process : memory){
            System.out.println("===================================Process "+process.getPcb().getProcessID()+"==========================");
            System.out.println("Executed "+completedInstructions.get(process.getPcb().getProcessID())+" instructions");
            System.out.println(++memoryLocation+" : a = "+process.getA());
            System.out.println(++memoryLocation+" : b = "+process.getB());
            System.out.println(++memoryLocation+" : c = "+process.getC());
            System.out.println(++memoryLocation+" : ID = "+process.getPcb().getProcessID());
            System.out.println(++memoryLocation+" : PC = "+process.getPcb().getPC());
            System.out.println(++memoryLocation+" : State = "+process.getPcb().getState());
            System.out.println(++memoryLocation+" : Instruction Start = "+process.getPcb().getStart());
            System.out.println(++memoryLocation+" : Instruction End = "+process.getPcb().getEnd());

            for (int i = 0 ; i < process.getInstructions().size() ; i++){
                if (process.getPcb().getPC() == i){
                    System.out.println(++memoryLocation+" : instruction = "+process.getInstructions().get(i) + "   <<<<<<<<<<< PC");
                }
                else
                    System.out.println(++memoryLocation+" : instruction = "+process.getInstructions().get(i));
            }
            System.out.println("=====================================================================");
            System.out.println();

        }

    }




    public String toString(){
        String str = "";
        str += "Cycle Number "+cycleNumber+"\n";
        str += "Number of processes = "+numberOfProcesses+"\n"+
        "Available Memory Space = "+availableMemorySpace+"\n"+
        "Ready Queue "+readyQueue+"\n";

        str += "Blocked Queue "+blockedQueue+"\n";

        System.out.println(str);
        System.out.println("Processes completed instructions per time slice "+completedInstructions);
        System.out.println("Processes in memory "+processesLocations);
        System.out.println("Processes on disk "+processesOnDisk);
        System.out.println("Input mutex blocked queue "+inputMutex.getWaitingProcesses());
        System.out.println("Output mutex blocked queue "+outputMutex.getWaitingProcesses());
        System.out.println("File mutex blocked queue "+fileMutex.getWaitingProcesses());

        displayMemoryContent();
        return "";
    }

    public static void main(String[] args) {


    }

}
