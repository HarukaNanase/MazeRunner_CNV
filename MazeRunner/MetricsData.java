
public class MetricsData {
    private long threadId = 0;
    private int instructionsRun = 0;
    private int basicBlocksFound = 0;
    private int methodsCount = 0;
    private int memoryCalls = 0;

    public MetricsData(){

    }

    public MetricsData(long threadId){
        this.threadId = threadId;
    }

    public int getInstructionsRun() {
        return instructionsRun;
    }

    public void setInstructionsRun(int instructionsRun) {
        this.instructionsRun = instructionsRun;
    }

    public int getBasicBlocksFound() {
        return basicBlocksFound;
    }

    public void setBasicBlocksFound(int basicBlocksFound) {
        this.basicBlocksFound = basicBlocksFound;
    }

    public int getMethodsCount() {
        return methodsCount;
    }

    public void setMethodsCount(int methodsCount) {
        this.methodsCount = methodsCount;
    }

    public int getMemoryCalls() {
        return memoryCalls;
    }

    public void setMemoryCalls(int memoryCalls) {
        this.memoryCalls = memoryCalls;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }






}
