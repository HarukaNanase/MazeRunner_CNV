import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;



@DynamoDBTable(tableName="metrics")
public class MetricsData {
    private long threadId = 0;
    private String UUID = null;
    private String requestQuery = null;
    private long instructionsRun = 0;
    private long basicBlocksFound = 0;
    private int methodsCount = 0;
    private int memoryCalls = 0;
    private int loopRuns = 0;

    private long observeBB = 0;
    private float percetangeObserve = 0;

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
    public long getInstructionsRun() {
        return instructionsRun;
    }

    public void setInstructionsRun(long instructionsRun) {
        this.instructionsRun = instructionsRun;
    }

    @DynamoDBAttribute(attributeName="bb_found")
    public long getBasicBlocksFound() {
        return basicBlocksFound;
    }

    public void setBasicBlocksFound(long basicBlocksFound) {
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

    @DynamoDBAttribute(attributeName="avgInstrPerBB")
    public float getAverageInstructionsPerBB(){
        return (float) this.getInstructionsRun()/this.getBasicBlocksFound();
    }

    @DynamoDBAttribute(attributeName="avgInstrPerStrategyRun")
    public float getAverageInstructionsPerLoop(){
        return (float) this.getInstructionsRun() / this.getLoopRuns();
    }


    @DynamoDBAttribute(attributeName = "ObserveBBs")
    public long getObserveBB() {
        return observeBB;
    }

    public void setObserveBB(long observeBB) {
        this.observeBB = observeBB;
    }

    @DynamoDBAttribute(attributeName = "ObserveBBPercentage")
    public float getPercetangeObserve() {
        return (float) observeBB / this.basicBlocksFound;
    }

    public void setPercetangeObserve(float percetangeObserve) {
        this.percetangeObserve = percetangeObserve;
    }
}
