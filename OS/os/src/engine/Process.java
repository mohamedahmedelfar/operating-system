package engine;

import java.io.Serializable;
import java.util.ArrayList;

public class Process implements Serializable {

    private Object a;
    private Object b;
    private Object c;

    private PCB pcb;
    private ArrayList<String> instructions;

    public Process(Object a, Object b, Object c, PCB pcb, ArrayList<String> instructions) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.pcb = pcb;

        this.instructions = new ArrayList<>();

        for(String str : instructions){
            this.instructions.add(str);
        }
    }


    public Object getA() {
        return a;
    }

    public void setA(Object a) {
        this.a = a;
    }

    public Object getB() {
        return b;
    }

    public void setB(Object b) {
        this.b = b;
    }

    public Object getC() {
        return c;
    }

    public void setC(Object c) {
        this.c = c;
    }

    public PCB getPcb() {
        return pcb;
    }

    public ArrayList<String> getInstructions() {
        return instructions;
    }


}
