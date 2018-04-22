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
    private int loopObserves = 0;
    private int velocity = 0;
    private long observeBB = 0;
    private float percetangeObserve = 0;

    private long runBB = 0;


    public MetricsData(){

    }

    public MetricsData(long threadId){
        this.threadId = threadId;
    }

    public MetricsData(long threadId, String requestQuery){
        this.threadId = threadId;
        this.requestQuery = requestQuery;
    }

    public MetricsData(long threadId, String requestQuery, int velo){
        this.threadId = threadId;
        this.requestQuery = requestQuery;
        this.velocity = velo;
    }

    @DynamoDBHashKey(attributeName = "UUID")
    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

   // @DynamoDBAttribute(attributeName="instructionsRun")
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

    @DynamoDBAttribute(attributeName="memoryAllocs")
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

    //@DynamoDBAttribute(attributeName="avgInstrPerBB")
    public float getAverageInstructionsPerBB(){
        return (float) this.getInstructionsRun()/this.getBasicBlocksFound();
    }

    @DynamoDBAttribute(attributeName="avgBBPerStrategyRun")
    public float getAverageInstructionsPerLoop(){
        return (float) this.getBasicBlocksFound() / this.getLoopRuns();
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
        return (float) observeBB / (this.basicBlocksFound+this.runBB + this.observeBB);
    }

    public void setPercetangeObserve(float percetangeObserve) {
        this.percetangeObserve = percetangeObserve;
    }
    @DynamoDBAttribute(attributeName = "RunBBs")
    public long getRunBB() {
        return runBB;
    }

    public void setRunBB(long runBB) {
        this.runBB = runBB;
    }

    @DynamoDBAttribute(attributeName = "RunBBPercentage")
    public float getPercentageRun(){
        return (float) runBB / (this.basicBlocksFound+this.runBB + this.observeBB);
    }

    public int getVelocity() {
        return velocity;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    @DynamoDBAttribute(attributeName = "EstimatedRunBBLs")
    public long getEstimatedRunBBL(){
        int first_sipush = 10000;
        int second_sipush = 4500;
        long first_parcel = (first_sipush / this.velocity) * second_sipush  * 3;
        long second_parcel = (first_sipush/this.velocity) * 4;
        long third_parcel = 2;

        return (first_parcel + second_parcel + third_parcel) * (this.loopRuns-1) + (this.loopRuns-1);
    }

    @DynamoDBAttribute(attributeName = "EstimatedObserveBBLs")
    public long getEstimatedObserveBBL(){
        int k = 0;
        if(this.requestQuery.contains("astar")){
            k = 5;
        }
        else if(this.requestQuery.contains("dfs")){
            k = 1;
        }
        else if(this.requestQuery.contains("bfs")){
            k = 3;
        }
        // k is a variable that depends in the strategy. This are estimated values as int from the bytecode i_const.
        int sipush = 1250; // from bytecode
        int third_param = 256; // from tests -> injected once and got a result of 256 loops inside the last for

        long first_parcel = (long) k * sipush * third_param * 2;
        long second_parcel = (long) k * sipush * 5;
        long third_parcel = (long) k * 4;
        long fourth_parcel = 3;

        //this magic numbers came from examination of the tool with injected code to count the basic blocks. we have then removed it from the tool
        //to avoid overhead.
        return (first_parcel + second_parcel + third_parcel + fourth_parcel) * this.getLoopObserves();


    }


    public int getLoopObserves() {
        return loopObserves;
    }

    public void setLoopObserves(int loopObserves) {
        this.loopObserves = loopObserves;
    }


}
