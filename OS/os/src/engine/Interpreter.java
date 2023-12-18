package engine;

import java.io.IOException;

public class Interpreter {

    private OperatingSystem operatingSystem ;

    public Interpreter(){
        operatingSystem = new OperatingSystem();
    }

    public OperatingSystem getOs(){
        return operatingSystem;
    }

    public void execute() throws IOException, ClassNotFoundException {
            Process currentProcess = operatingSystem.chooseProcess();
            int processId = currentProcess.getPcb().getProcessID();

            if (currentProcess.getPcb().getPC() >= currentProcess.getInstructions().size()){
                operatingSystem.getReadyQueue().removeFirst();
                System.out.println(operatingSystem);
                operatingSystem.incrementClockCycles();
                return;
            }
            String line = currentProcess.getInstructions().get(currentProcess.getPcb().getPC());
            String[] instruction = line.split(" ");

            System.out.println("Executing instruction [ "+line+" ] From Process "+currentProcess.getPcb().getProcessID());
            if (instruction[0].equals("assign")) {
                if (instruction.length == 3) {

                    if(operatingSystem.getProcessesInput().containsKey(processId)){
                        System.out.println("Process "+processId+" is assignning "+instruction[1]+" to input");
                        operatingSystem.writeToMemory(instruction[1], processId,operatingSystem.getProcessesInput().get(processId));
                        operatingSystem.getProcessesInput().remove(processId);
                    }
                    else {
                        System.out.println("Process "+processId+" is taking a user input ");
                        Object input = operatingSystem.takeUserInput();
                        operatingSystem.getProcessesInput().put(processId,input);
                    }
                }
                else {
                    if(operatingSystem.getProcessesInput().containsKey(processId)){
                        System.out.println("Process "+processId+" is assignning "+instruction[1]+" to data in file "+instruction[3]);
                        operatingSystem.writeToMemory(instruction[1],processId,operatingSystem.getProcessesInput().get(processId));
                        operatingSystem.getProcessesInput().remove(processId);

                    }
                    else {
                        System.out.println("Process "+processId+" is reading file "+instruction[3]);
                        Object fileData = operatingSystem.readFile(instruction[3],processId);
                        operatingSystem.getProcessesInput().put(processId,fileData);
                    }
                }

            }
            else if (instruction[0].equals("print")) {
                operatingSystem.print(instruction[1],processId);
            }
            else if (instruction[0].equals("printFromTo")) {
                operatingSystem.printFromTo(instruction[1],instruction[2],processId);
            }

            else if (instruction[0].equals("writeFile")) {
                operatingSystem.writeFile(instruction[1],instruction[2],processId);
            }
            else if (instruction[0].equals("semWait")) {
                operatingSystem.semWait(processId,instruction[1]);
            }
            else if (instruction[0].equals("semSignal")) {
                operatingSystem.semSignal(processId,instruction[1]);
            }
            System.out.println(operatingSystem);
            operatingSystem.incrementClockCycles();


    }

    public static void startInterpreter() throws IOException, ClassNotFoundException {
        Interpreter interpreter = new Interpreter();
        interpreter.getOs().createProcess("src/Program_1.txt");
        System.out.println(interpreter.getOs());
        interpreter.getOs().incrementClockCycles();
        int i = 1;
        while (! interpreter.getOs().checkAllProcessesFinished()) {
            if (i == 1){
                interpreter.getOs().createProcess("src/Program_2.txt");
                System.out.println(interpreter.getOs());
                interpreter.getOs().incrementClockCycles();

            }
            else if (i == 4){
                interpreter.getOs().createProcess("src/Program_3.txt");
                System.out.println(interpreter.getOs());
                interpreter.getOs().incrementClockCycles();

            }
            else
                interpreter.execute();
            i++;
        }
        System.out.println("All the processes are done!");
    }


    public static void main(String[] args) throws IOException, ClassNotFoundException {
        startInterpreter();
    }

}
