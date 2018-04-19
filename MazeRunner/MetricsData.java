import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;



@DynamoDBTable(tableName="metrics")
public class MetricsData {
    private long threadId = 0;
    private String UUID = null;
    private String requestQuery = null;
    private int instructionsRun = 0;
    private int basicBlocksFound = 0;
    private int methodsCount = 0;
    private int memoryCalls = 0;
    private int loopRuns = 0;

    public MetricsData(){

    }

    public MetricsData(long threadId){
        this.threadId = threadId;
    }

    public MetricsData(long threadId, String requestQuery){
        this.threadId = threadId;
        this.requestQuery = requestQuery;
    }

    @DynamoDBHashKey(attributeName = "UUID")
    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    @DynamoDBAttribute(attributeName="instructionsRun")
    public int getInstructionsRun() {
        return instructionsRun;
    }

    public void setInstructionsRun(int instructionsRun) {
        this.instructionsRun = instructionsRun;
    }

    @DynamoDBAttribute(attributeName="bb_found")
    public int getBasicBlocksFound() {
        return basicBlocksFound;
    }

    public void setBasicBlocksFound(int basicBlocksFound) {
        this.basicBlocksFound = basicBlocksFound;
    }

    @DynamoDBAttribute(attributeName="methodsCount")
    public int getMethodsCount() {
        return methodsCount;
    }

    public void setMethodsCount(int methodsCount) {
        this.methodsCount = methodsCount;
    }

    @DynamoDBAttribute(attributeName="memoryCalls")
    public int getMemoryCalls() {
        return memoryCalls;
    }

    public void setMemoryCalls(int memoryCalls) {
        this.memoryCalls = memoryCalls;
    }

    @DynamoDBAttribute(attributeName="threadId")
    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    @DynamoDBAttribute(attributeName="requestQuery")
    public String getRequestQuery() {
        return requestQuery;
    }

    public void setRequestQuery(String requestQuery) {
        this.requestQuery = requestQuery;
    }

    @DynamoDBAttribute(attributeName="loopRuns")
    public int getLoopRuns() {
        return loopRuns;
    }

    public void setLoopRuns(int loopRuns) {
        this.loopRuns = loopRuns;
    }

}
