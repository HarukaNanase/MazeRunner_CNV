import BIT.highBIT.*;

import java.io.PrintStream;
import java.util.Date;
import java.util.Enumeration;

import BIT.lowBIT.CONSTANT_Methodref_Info;
import BIT.lowBIT.CONSTANT_NameAndType_Info;
import BIT.lowBIT.CONSTANT_Utf8_Info;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import java.util.Map;
import java.util.concurrent.SynchronousQueue;

public class TestMetrics {

    private static Date StartTime = null;
    private static Date EndTime = null;
    private static PrintStream out = null;
    private static int bb_count = 0;
    private static int instr_count = 0;
    private static int method_count = 0;
    private static int first_for;
    private static int second_for;
    static boolean firstIsDone = false;
    private static String maze_arguments = null;
    private static DynamoDBMapper mapper = null;
    public static void main(String[] argv) {
        String infilename = argv[0];
        infilename += ".class";
        if (infilename.endsWith(".class")) {
            String classname = infilename.substring(infilename.lastIndexOf("\\") + 1);
            ClassInfo ci = new ClassInfo(infilename);
            if (classname.equals("Main.class")) {
                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    InjectDynamicMETHODCount(routine);
                    InjectDynamicINSTCount(routine);
                    InjectDynamicMEMCount(routine);
                }
                ci.addBefore("TestMetrics", "StartTimer", new Integer(0));
                ci.addAfter("TestMetrics", "EndTimer", new Integer(0));
            } else if (classname.equals("AStarStrategy.class")) {
                InjectIntoStrategy(ci);
            }

            else if (classname.equals("BreadthFirstSearchStrategy.class")) {
                InjectIntoStrategy(ci);
            }else if(classname.equals("DepthFirstSearchStrategy.class")){
                InjectIntoStrategy(ci);
            }else if(classname.equals("Coordinate.class")){
               // InjectIntoCoordinate(ci);
            }else if(classname.equals("RobotController.class"))
            {
                InjectRobotController(ci);
            }
            else if(classname.equals("Maze.class")){
                for(Enumeration e = ci.getRoutines().elements(); e.hasMoreElements();){
                    Routine routine = (Routine) e.nextElement();
                    //InjectDynamicINSTCount(routine);
                }
            }
            ci.write(argv[1] + System.getProperty("file.separator") + infilename.substring(infilename.lastIndexOf("\\") + 1));
        }
    }


    static synchronized void InjectDynamicMEMCount(Routine routine){
        for (Instruction instruction : routine.getInstructions()) {
            int opcode = instruction.getOpcode();
            if ((opcode == InstructionTable.NEW) ||
                    (opcode == InstructionTable.newarray) ||
                    (opcode == InstructionTable.anewarray) ||
                    (opcode == InstructionTable.multianewarray)) {
                instruction.addBefore("TestMetrics", "MEMCount", new Integer(opcode));
            }
        }
    }

    static synchronized void InjectDynamicINSTCount(Routine routine){
        for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
            BasicBlock bb = (BasicBlock) b.nextElement();
            bb.addBefore("TestMetrics", "dynINSTCount", new Integer(bb.size()));
        }
    }

    public static synchronized void dynINSTCount(int instr_count){
        MetricsData metricsThread = WebServer.getHashMap().get(Thread.currentThread().getId());
       // metricsThread.setInstructionsRun(metricsThread.getInstructionsRun()+instr_count);
        metricsThread.setBasicBlocksFound(metricsThread.getBasicBlocksFound()+1);
    }

    public static synchronized void dynMETHCount(int meth_size){
        MetricsData metricsThread = WebServer.getHashMap().get(Thread.currentThread().getId());
        metricsThread.setMethodsCount(metricsThread.getMethodsCount()+meth_size);
    }

    static synchronized void InjectDynamicMETHODCount(Routine routine){
        routine.addBefore("TestMetrics", "dynMETHCount", new Integer(1));
    }



    static synchronized void InjectRobotController(ClassInfo ci){
        //MetricsData metrics = WebServer.getHashMap().get(Thread.currentThread().getId());
        for(Enumeration e = ci.getRoutines().elements(); e.hasMoreElements();){
            Routine routine = (Routine) e.nextElement();
            //InjectDynamicMETHODCount(routine);
            //InjectDynamicINSTCount(routine);


            if(routine.getMethodName().equals("run")) {
                for(BasicBlock bb : routine.getBasicBlocks().getBasicBlocks()){
                    //bb.addBefore("TestMetrics", "RobotControllerRunCount", new Integer(1));
                }
            }
            if(routine.getMethodName().equals("observe")){
                for(BasicBlock bb : routine.getBasicBlocks().getBasicBlocks()){
                   // bb.addBefore("TestMetrics", "RobotControlerObserveCount", new Integer(1));
                }


            }
        }
    }
    public static synchronized void RobotControllerRunCount(int run){
        MetricsData metricsThread = WebServer.getHashMap().get(Thread.currentThread().getId());
        metricsThread.setRunBB(metricsThread.getRunBB()+1);
    }



    public static synchronized void RobotControlerObserveCount(int bb_count){
        MetricsData metricsThread = WebServer.getHashMap().get(Thread.currentThread().getId());
        metricsThread.setObserveBB(metricsThread.getObserveBB()+1);
    }


    private static void InjectIntoStrategy(ClassInfo ci){
        for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
            Routine routine = (Routine) e.nextElement();
            InjectDynamicMETHODCount(routine);
            InjectDynamicINSTCount(routine);
            if (routine.getMethodName().equals("run")) {
                //routine.addBefore("TestMetrics", "INSTRCount", new Integer(routine.getInstructionCount()));
                for (Instruction instr : routine.getInstructions()) {
                    int opcode = instr.getOpcode();
                    if (instr.getOpcode() == InstructionTable.invokestatic) {
                        if (routine.getConstantPool()[instr.getOperandValue()] instanceof CONSTANT_Methodref_Info) {
                            short nameTypeIndex = ((CONSTANT_Methodref_Info) routine.getConstantPool()[instr.getOperandValue()]).name_and_type_index;
                            CONSTANT_NameAndType_Info nameTypeInfo = (CONSTANT_NameAndType_Info) routine.getConstantPool()[nameTypeIndex];
                            short nameIndex = nameTypeInfo.name_index;
                            if (routine.getConstantPool()[nameIndex] instanceof CONSTANT_Utf8_Info) {
                                byte[] name = ((CONSTANT_Utf8_Info) routine.getConstantPool()[nameIndex]).bytes;
                                try {
                                    String s = new String(name, "UTF-8");
                                    if (s.equals("observe")) {
                                        System.out.println("Found Observe Index: " + instr.getOperandValue());
                                        instr.addBefore("TestMetrics", "StrategyObserves", new Integer(1));
                                    }
                                    else if(s.equals("run")){
                                        System.out.println("Found Run Index: " + instr.getOperandValue());
                                        instr.addBefore("TestMetrics", "StrategyRuns", new Integer(1));
                                    }

                                } catch (Exception b) {
                                    b.getMessage();
                                }
                            }
                        }
                    } else if ((opcode == InstructionTable.NEW) ||
                            (opcode == InstructionTable.newarray) ||
                            (opcode == InstructionTable.anewarray) ||
                            (opcode == InstructionTable.multianewarray)) {
                        instr.addBefore("TestMetrics", "MEMCount", new Integer(opcode));
                    }
                }
            }else if(routine.getMethodName().equals("solveAux") &&
                    ci.getClassName().substring(ci.getClassName().lastIndexOf("/")+1).equals("DepthFirstSearchStrategy")){
                        //routine.addBefore("TestMetrics", "StrategyRuns", new Integer(1));
                for (Instruction instr : routine.getInstructions()) {
                    int opcode = instr.getOpcode();
                    if (instr.getOpcode() == InstructionTable.invokestatic) {
                        if (routine.getConstantPool()[instr.getOperandValue()] instanceof CONSTANT_Methodref_Info) {
                            short nameTypeIndex = ((CONSTANT_Methodref_Info) routine.getConstantPool()[instr.getOperandValue()]).name_and_type_index;
                            CONSTANT_NameAndType_Info nameTypeInfo = (CONSTANT_NameAndType_Info) routine.getConstantPool()[nameTypeIndex];
                            short nameIndex = nameTypeInfo.name_index;
                            if (routine.getConstantPool()[nameIndex] instanceof CONSTANT_Utf8_Info) {
                                byte[] name = ((CONSTANT_Utf8_Info) routine.getConstantPool()[nameIndex]).bytes;
                                try {
                                    String s = new String(name, "UTF-8");
                                    if (s.equals("run")) {
                                        System.out.println("Found Observe Index: " + instr.getOperandValue());
                                        instr.addBefore("TestMetrics", "StrategyRuns", new Integer(1));
                                    }
                                    else if (s.equals("observe")) {
                                        System.out.println("Found Observe Index: " + instr.getOperandValue());
                                        instr.addBefore("TestMetrics", "StrategyObserves", new Integer(1));
                                    }

                                } catch (Exception b) {
                                    b.getMessage();
                                }
                            }
                        }
                    }}
            }
        }
    }

    private static synchronized void InjectIntoCoordinate(ClassInfo ci) {
        for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
            Routine routine = (Routine) e.nextElement();
//            InjectDynamicMETHODCount(routine);
//            InjectDynamicINSTCount(routine);
            if (routine.getMethodName().equals("getUnvisitedNeighboors")) {
                for (Instruction instr : routine.getInstructions()) {
                    int opcode = instr.getOpcode();
                    if ((opcode == InstructionTable.NEW) ||
                            (opcode == InstructionTable.newarray) ||
                            (opcode == InstructionTable.anewarray) ||
                            (opcode == InstructionTable.multianewarray)) {
                        instr.addBefore("TestMetrics", "MEMCount", new Integer(1));
                    }
                }
            }
        }
    }

    public static synchronized void StartTimer(int i){
        StartTime = new Date();
    }

    public static synchronized void EndTimer(int i){
        EndTime = new Date();
        System.out.println("Time taken to solve maze: " + ((EndTime.getTime() - StartTime.getTime())/1000) + " Seconds");
    }

    public static synchronized void BBCount(int bb_size){
        MetricsData metricsThread = WebServer.getHashMap().get(Thread.currentThread().getId());
        metricsThread.setBasicBlocksFound(metricsThread.getBasicBlocksFound()+bb_size);
        //System.out.println(metricsThread.getThreadId());
    }

    public static synchronized void INSTRCount(int instr_size) {
        MetricsData metricsThread = WebServer.getHashMap().get(Thread.currentThread().getId());
        metricsThread.setInstructionsRun(metricsThread.getInstructionsRun() + instr_size);

    }

    public static synchronized void METHODCount(int meth_size){
        MetricsData metricsThread = WebServer.getHashMap().get(Thread.currentThread().getId());
        metricsThread.setMethodsCount(metricsThread.getMethodsCount() + meth_size);
    }

    public static synchronized void MEMCount(int opcode){
        MetricsData metricsThread = WebServer.getHashMap().get(Thread.currentThread().getId());
        metricsThread.setMemoryCalls(metricsThread.getMemoryCalls() + 1);
    }

    public static synchronized void StrategyRuns(int loop_add){
        MetricsData metricsThread = WebServer.getHashMap().get(Thread.currentThread().getId());
        metricsThread.setLoopRuns(metricsThread.getLoopRuns() + loop_add);
    }

    public static synchronized void StrategyObserves(int loop_add){
        MetricsData metricsThread = WebServer.getHashMap().get(Thread.currentThread().getId());
        metricsThread.setLoopObserves(metricsThread.getLoopObserves() + loop_add);
    }

}
