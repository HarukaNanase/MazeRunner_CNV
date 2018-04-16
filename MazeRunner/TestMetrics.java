import BIT.highBIT.*;

import java.io.PrintStream;
import java.util.Date;
import java.util.Enumeration;

public class TestMetrics {

    private static Date StartTime = null;
    private static Date EndTime = null;
    private static PrintStream out = null;
    private static int bb_count = 0;
    private static int instr_count = 0;
    private static int method_count = 0;
    private static String maze_arguments = null;

    public static void main(String[] argv){

            String infilename = argv[0];
            infilename += ".class";
                if (infilename.endsWith(".class")) {
                    ClassInfo ci = new ClassInfo(infilename);

                    for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                        Routine routine = (Routine) e.nextElement();
                        routine.addBefore("TestMetrics", "INSTRCount", new Integer(routine.getInstructionCount()));
                        for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                            BasicBlock bb = (BasicBlock) b.nextElement();
                            bb.addBefore("TestMetrics", "BBCount", new Integer(bb.size()));
                        }
                    }
                    ci.addAfter("TestMetrics", "PrintInfo", ci.getClassName());
                    ci.write(argv[1] + System.getProperty("file.separator") + infilename.substring(infilename.lastIndexOf("\\")+1));
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
        bb_count += bb_size;
        MetricsData metricsThread = WebServer.getHashMap().get(Thread.currentThread().getId());
        metricsThread.setBasicBlocksFound(bb_count);
        //System.out.println(metricsThread.getThreadId());
    }

    public static synchronized void INSTRCount(int instr_size) {
        instr_count += instr_size;
        MetricsData metricsThread = WebServer.getHashMap().get(Thread.currentThread().getId());
        metricsThread.setInstructionsRun(instr_count);

    }

    public static synchronized void METHODCount(int meth_size){
        method_count += meth_size;
        MetricsData metricsThread = WebServer.getHashMap().get(Thread.currentThread().getId());
        metricsThread.setMethodsCount(method_count);
    }


    public static synchronized void PrintInfo(String in){
        WebServer.PrintHashMap();
    }






}
