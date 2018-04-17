import BIT.highBIT.*;

import java.io.PrintStream;
import java.util.Date;
import java.util.Enumeration;
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
                    ci.addAfter("TestMetrics", "ResetMetricsData", ci.getClassName());
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
        System.out.println("INSTRCount: Current instr count " + instr_count);
        MetricsData metricsThread = WebServer.getHashMap().get(Thread.currentThread().getId());
        metricsThread.setInstructionsRun(instr_count);

    }

    public static synchronized void METHODCount(int meth_size){
        method_count += meth_size;
        MetricsData metricsThread = WebServer.getHashMap().get(Thread.currentThread().getId());
        metricsThread.setMethodsCount(method_count);
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

    }

    public static synchronized void ResetMetricsData(String in){
        //save to dynamo
        MetricsData metrics = WebServer.getHashMap().get(Thread.currentThread().getId());
        metrics.setBasicBlocksFound(0);
        metrics.setInstructionsRun(0);
        metrics.setMemoryCalls(0);
        metrics.setMethodsCount(0);
        WebServer.getHashMap().remove(Thread.currentThread().getId());
        bb_count = 0;
        System.out.println("TestMetrics: Total instructions run: " + instr_count);
        method_count = 0;
        instr_count = 0;

    }






}
