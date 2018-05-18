import Heuristics.IHeuristic;
import com.amazonaws.services.dynamodbv2.datamodeling.*;

import java.util.Objects;


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
    private String MazeType = null;
    private long runBB = 0;
    private long branches_taken = 0;
    private int x0 = 0;
    private int x1 = 0;
    private int y0 = 0;
    private int y1 = 0;
    private String strategy = null;
    private int jobId = 0;
    private double expectedWorkload = 0;
    private long expectedBranches = 0;
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

    @DynamoDBHashKey(attributeName = "MazeType")
    public String getMazeType(){
        return this.MazeType;
    }

    @DynamoDBRangeKey(attributeName = "UUID")
    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }


    public void setMazeType(String maze){
      this.MazeType = maze;
    }
   // @DynamoDBAttribute(attributeName="instructionsRun")
   @DynamoDBIgnore
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

    @DynamoDBIgnore
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

    @DynamoDBIgnore
    public int getLoopRuns() {
        return loopRuns;
    }

    public void setLoopRuns(int loopRuns) {
        this.loopRuns = loopRuns;
    }

    @DynamoDBIgnore
    public float getAverageInstructionsPerBB(){
        return (float) this.getInstructionsRun()/this.getBasicBlocksFound();
    }

    @DynamoDBIgnore
    public float getAverageInstructionsPerLoop(){
        return (float) this.getBasicBlocksFound() / this.getLoopRuns();
    }

    @DynamoDBIgnore
    public long getObserveBB() {
        return observeBB;
    }

    public void setObserveBB(long observeBB) {
        this.observeBB = observeBB;
    }

    @DynamoDBIgnore
    public float getPercetangeObserve() {
        return (float) observeBB / (this.basicBlocksFound+this.runBB + this.observeBB);
    }

    public void setPercetangeObserve(float percetangeObserve) {
        this.percetangeObserve = percetangeObserve;
    }

    @DynamoDBIgnore
    public long getRunBB() {
        return runBB;
    }

    public void setRunBB(long runBB) {
        this.runBB = runBB;
    }

    @DynamoDBIgnore
    public float getPercentageRun(){
        return (float) runBB / (this.basicBlocksFound+this.runBB + this.observeBB);
    }

    public int getVelocity() {
        return velocity;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }
    @DynamoDBIgnore
    public long getEstimatedRunBBL(){
        int first_sipush = 10000;
        int second_sipush = 4500;
        long first_parcel = (first_sipush / this.velocity) * second_sipush  * 3;
        long second_parcel = (first_sipush/this.velocity) * 4;
        long third_parcel = 2;

        return (first_parcel + second_parcel + third_parcel) * (this.loopRuns) + (this.loopRuns);
    }
    @DynamoDBIgnore
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

    @DynamoDBIgnore
    public int getLoopObserves() {
        return loopObserves;
    }

    public void setLoopObserves(int loopObserves) {
        this.loopObserves = loopObserves;
    }

    @DynamoDBAttribute(attributeName="BranchesTaken")
    public long getBranches_taken(){ return this.branches_taken; }
    public void setBranches_taken(long taken){ this.branches_taken = taken; }

    @DynamoDBAttribute(attributeName="x0")
    public int getX0() {
        return x0;
    }

    public void setX0(int x0) {
        this.x0 = x0;
    }
    @DynamoDBAttribute(attributeName="x1")
    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    @DynamoDBAttribute(attributeName="y0")
    public int getY0() {
        return y0;
    }

    public void setY0(int y0) {
        this.y0 = y0;
    }

    @DynamoDBAttribute(attributeName="y1")
    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    @DynamoDBIgnore
    public int getCalculatedWorkload(){
        //TO DO: Implement this method to calculate a workload for a group of metrics based on their parameters;
        return 0;
    }

    @DynamoDBAttribute(attributeName="strategy")
    public String getStrategy(){
        return this.strategy;
    }

    public void setStrategy(String strat){
        this.strategy = strat;
    }

    @DynamoDBAttribute(attributeName="workload")
    public double getWorkload(IHeuristic heuristic){
        return heuristic.getWorkload(this.branches_taken, this.basicBlocksFound);
    }

    @DynamoDBIgnore
    public int getJobId(){
        return this.jobId;
    }

    public void setJobId(int id){
        this.jobId = id;
    }

    @DynamoDBAttribute(attributeName="expectedWorkload")
    public double getExpectedWorkload(){
        return this.expectedWorkload;
    }

    public void setExpectedWorkload(double work){
        this.expectedWorkload = work;
    }

    @DynamoDBIgnore
    public long getExpectedBranches(){
        return this.expectedBranches;
    }

    public void setExpectedBranches(long b){
        this.expectedBranches = b;
    }


    @Override
    public boolean equals(Object t){
        if(t instanceof MetricsData){
            MetricsData target = (MetricsData) t;
            if(this.getStrategy().equals(target.getStrategy()) && this.getMazeType().equals(target.getMazeType())
                    && this.getX0() == target.getX0() && this.getX1() == target.getX1() && this.getY0() == target.getY0()
                    && this.getY1() == target.getY1())
                return true;
        }

        return false;
    }

}
