import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class EC2Machine {


    private String REQUESTCOUNT_ENDPOINT = "/mazecount";
    private String SOLVER_ENDPOINT = "/mzrun.html";
    private String ALIVE_ENDPOINT = "/alive";
    private String PROTOCOL = "http://";
    private int MIN_COUNT = 1;
    private int MAX_COUNT = 1;
    private Set<Instance> instances;
    public AmazonEC2 ec2;
    private AmazonCloudWatch cloudWatch;
    private String publicDNS = null;
    private String instanceId = null;
    private AWSCredentials credentials = null;
    private boolean terminateFlag = false;
    private int timeSinceLastFlag = 0;
    private ArrayList<Integer> jobs;
    public EC2Machine() {
        instances = new HashSet<Instance>();
        jobs = new ArrayList<Integer>();
    }

    private void init(String REGION) throws Exception {
        try {
            if(credentials == null)
                credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        if(ec2 == null)
            ec2 = AmazonEC2ClientBuilder.standard().withRegion(REGION).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
        if(cloudWatch == null)
            cloudWatch = AmazonCloudWatchClientBuilder.standard().withRegion(REGION).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }

    public String getPublicDNS(){
        return this.publicDNS;
    }

    public String getInstanceId(){
        return this.instanceId;
    }

    public void launchMachine(String AMI, String InstanceType, String KeyName,String SecurityGroup, String Region) throws Exception{
        this.init(Region);
        RunInstancesRequest runInstancesRequest =
                new RunInstancesRequest();
        runInstancesRequest.withImageId(AMI)
                .withInstanceType(InstanceType)
                .withMinCount(MIN_COUNT)
                .withMaxCount(MAX_COUNT)
                .withKeyName(KeyName)
                .withSecurityGroups(SecurityGroup);

        RunInstancesResult runInstancesResult =
                ec2.runInstances(runInstancesRequest);

        instanceId = runInstancesResult.getReservation().getInstances()
                .get(0).getInstanceId();

        DescribeInstancesResult describeInstancesResult = ec2.describeInstances();
        List<Reservation> reservations = describeInstancesResult.getReservations();
        for (Reservation reservation : reservations) {
            instances.addAll(reservation.getInstances());
        }

        this.publicDNS = getInstancePublicDNS();
        while(!getInstanceReadyStatus(instanceId))
            Thread.sleep(5000);
        System.out.println(publicDNS);
    }

    public boolean getInstanceReadyStatus(String instanceId){
        DescribeInstanceStatusRequest describeInstanceRequest = new DescribeInstanceStatusRequest().withInstanceIds(instanceId);
        DescribeInstanceStatusResult describeInstanceResult = ec2.describeInstanceStatus(describeInstanceRequest);
        List<InstanceStatus> state = describeInstanceResult.getInstanceStatuses();
        if(state.size() < 1){
            try {
                Thread.sleep(5000);
            }catch(InterruptedException ie){

            }
            return getInstanceReadyStatus(instanceId);
        }
        HttpURLConnection conn = null;
        try {
            //Create connection
            URL url = new URL(PROTOCOL + this.publicDNS + ALIVE_ENDPOINT);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String counterValue = rd.readLine();
            System.out.println("Machine " + instanceId + " is ready.");
            return (!counterValue.equals(""));
        } catch (Exception e) {
            return false;
        }

    }

    public String getInstancePublicDNS(){
        String pub = null;
        while(pub == null) {
            DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
            List<Reservation> reservations = describeInstancesRequest.getReservations();
            Set<Instance> allInstances = new HashSet<Instance>();
            for (Reservation reservation : reservations) {
                for (Instance instance : reservation.getInstances()) {
                    if (instance.getInstanceId().equals(instanceId))
                        pub = instance.getPublicDnsName();
                }
            }

            try {
                Thread.sleep(5000);
            }catch(Exception e){
                //
            }
        }
        if(pub.equals(""))
            pub = getInstancePublicDNS();

        return pub;
    }

    public int getServerRequestCount() {
       String count = HTTPRequest.GETRequestAsString(PROTOCOL +this.publicDNS + REQUESTCOUNT_ENDPOINT);
       return count != null ? Integer.parseInt(count) : -1;
    }

    public byte[] getMazeSolution(String request){
        return HTTPRequest.GETRequest(PROTOCOL + this.publicDNS + SOLVER_ENDPOINT + "?" + request);
    }


    public int getMachineWorkLoad(){
        //TO DO: Implement calculations for the machine workload.
        return 0;
    }

    public boolean terminateMachine(){
        //int requestCount = getServerRequestCount();
        int requestCount = getServerRequestCount();
        System.out.println("Trying to shut down machine. RequestCount: " + requestCount);
        if(requestCount == 0) {
            TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
            termInstanceReq.withInstanceIds(instanceId);
            ec2.terminateInstances(termInstanceReq);
            System.out.println("Killing machine: " + this.publicDNS);
            return true;
        }
        return false;
    }

    public void getMachineStatistics(){
        long offsetInMilliseconds = 1000 * 60 * 10;
        Dimension instanceDimension = new Dimension();
        instanceDimension.setName("InstanceId");
        List<Dimension> dims = new ArrayList<Dimension>();
        dims.add(instanceDimension);
        for (Instance instance : instances) {
            String name = instance.getInstanceId();
            String state = instance.getState().getName();
            if (state.equals("running")) {
                System.out.println("running instance id = " + name);
                instanceDimension.setValue(name);
                GetMetricStatisticsRequest request = new GetMetricStatisticsRequest()
                        .withStartTime(new Date(new Date().getTime() - offsetInMilliseconds))
                        .withNamespace("AWS/EC2")
                        .withPeriod(60)
                        .withMetricName("CPUUtilization")
                        .withStatistics("Average")
                        .withDimensions(instanceDimension)
                        .withEndTime(new Date());
                GetMetricStatisticsResult getMetricStatisticsResult =
                        cloudWatch.getMetricStatistics(request);
                List<Datapoint> datapoints = getMetricStatisticsResult.getDatapoints();
                for (Datapoint dp : datapoints) {
                    System.out.println(" CPU utilization for instance " + name +
                            " = " + dp.getAverage());
                }
            }
            else {
                System.out.println("instance id = " + name);
            }
            System.out.println("Instance State : " + state +".");
        }
    }


    public boolean getTerminateFlag(){
        return this.terminateFlag;
    }

    public void setTerminateFlag(boolean flag){
        this.terminateFlag = flag;
        if(!flag)
            this.timeSinceLastFlag = 0;
    }

    public int getTimeSinceLastFlag(){
        return this.timeSinceLastFlag;
    }

    public void setTimeSinceLastFlag(int time){
        this.timeSinceLastFlag = time;
    }

    public ArrayList<Integer> getJobs(){
        return this.jobs;
    }

}
