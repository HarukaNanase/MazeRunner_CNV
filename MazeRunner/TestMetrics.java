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

public class TestMetrics {

    private static Date StartTime = null;
    private static Date EndTime = null;
    private static PrintStream out = null;
    private static int bb_count = 0;
    private static int instr_count = 0;
    private static int method_count = 0;
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
                    routine.addBefore("TestMetrics", "INSTRCount", new Integer(routine.getInstructionCount()));
                    for (Instruction instruction : routine.getInstructions()) {
                        int opcode = instruction.getOpcode();
                        if ((opcode == InstructionTable.NEW) ||
                                (opcode == InstructionTable.newarray) ||
                                (opcode == InstructionTable.anewarray) ||
                                (opcode == InstructionTable.multianewarray)) {
                            instruction.addBefore("TestMetrics", "MEMCount", new Integer(opcode));
                        }
                    }

                    for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        bb.addBefore("TestMetrics", "BBCount", new Integer(bb.size()));
                    }
                }
                ci.addAfter("TestMetrics", "PrintInfo", ci.getClassName());
                ci.addAfter("TestMetrics", "ResetMetricsData", ci.getClassName());
            } else if (classname.equals("AStarStrategy.class")) {
                InjectIntoStrategy(ci);
            }

            else if (classname.equals("BreadthFirstSearchStrategy.class")) {
                InjectIntoStrategy(ci);
            }else if(classname.equals("DepthFirstSearchStrategy.class")){
                InjectIntoStrategy(ci);
            }

            ci.write(argv[1] + System.getProperty("file.separator") + infilename.substring(infilename.lastIndexOf("\\") + 1));
        }
    }



    private static void InjectIntoStrategy(ClassInfo ci){
        for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
            Routine routine = (Routine) e.nextElement();
            if (routine.getMethodName().equals("run")) {
                routine.addBefore("TestMetrics", "INSTRCount", new Integer(routine.getInstructionCount()));
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
                                        instr.addBefore("TestMetrics", "StrategyRuns", new Integer(1));
                                    }
                                    if(s.equals("solveAux")){
                                        System.out.println("Found solveAux Index: " + instr.getOperandValue());
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

    public static synchronized void PrintInfo(String in){
        try{
            DynamoController.init();
            MetricsData metricsThread = WebServer.getHashMap().get(Thread.currentThread().getId());
            if(mapper == null)
                mapper = new DynamoDBMapper(DynamoController.dynamoDB);
            mapper.save(metricsThread);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

        MetricsData metricsThread = WebServer.getHashMap().get(Thread.currentThread().getId());
        System.out.println("Thread " + metricsThread.getThreadId() + " Metrics Data:");
        System.out.println("Request for this thread: " + metricsThread.getRequestQuery());
        System.out.println("Instructions Run: " + metricsThread.getInstructionsRun());
        System.out.println("BasicBlocks Found: " + metricsThread.getBasicBlocksFound());
        System.out.println("Methods Count: " + metricsThread.getMethodsCount());
        System.out.println("Memory Calls: " + metricsThread.getMemoryCalls());
        System.out.println("Strategy Runs: " + metricsThread.getLoopRuns());
    }

    public static synchronized void ResetMetricsData(String in){
        //save to dynamo
        MetricsData metrics = WebServer.getHashMap().get(Thread.currentThread().getId());

        System.out.println("TestMetrics: Total instructions run: " + metrics.getInstructionsRun());
        metrics.setBasicBlocksFound(0);
        metrics.setInstructionsRun(0);
        metrics.setMemoryCalls(0);
        metrics.setMethodsCount(0);
        metrics.setLoopRuns(0);
        WebServer.getHashMap().remove(Thread.currentThread().getId());
        bb_count = 0;
        method_count = 0;
        instr_count = 0;

    }






}
