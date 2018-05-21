import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class EC2AutoScaler {

    private Map<String, EC2Machine> machines;
    private int minimumMachines = 1;
    private int maximumMachines = 20;
    private int MAXIMUM_HEAVYNESS = 85;
    private String REGION = "us-east-1";
    private String IMAGE_ID = "ami-067f4fc6a3d7c79f6";
    private String INSTANCE_TYPE = "t2.micro";
    private String KEY_NAME = "CNV_AWS";
    private String SECURITY_GROUP = "CNV-HTTP-SSH";
    private int WORKLOAD_THRESHOLD = 30;
    private double WORKLOAD_COEFFICIENT = 0.8;
    private int MACHINE_KEEP_ALIVE = 10;//1*60;
    private int SYSTEM_CHECK_TIME = 5;
    private int machinesGoingOnline = 0;
    private int MAX_IDLE_TIME = 2*60*1000;//2*60*1000;
    private int MAXIMUM_FAILED_HEALTH_CHECKS = 5;
    //private AtomicBoolean canBootPreemptiveMachine;
    public EC2AutoScaler(){

        this.machines = new ConcurrentHashMap<String, EC2Machine>();
      //  canBootPreemptiveMachine = new AtomicBoolean(true);
    }


    public EC2Machine createNewMachine(String IMAGE_ID, String INSTANCE_TYPE, String KEY_NAME, String SECURITY_GROUP, String REGION){
        if(this.machines.size() < this.maximumMachines) {
            EC2Machine machine = new EC2Machine();
            try {
                machine.launchMachine(IMAGE_ID, INSTANCE_TYPE, KEY_NAME, SECURITY_GROUP, REGION);
                this.machines.put(machine.getInstanceId(), machine);
                System.out.println("EC2AutoScaler Scaling Up: Current System Size: " + this.machines.size());
                return machine;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public Map<String, EC2Machine> getMachines(){
        return this.machines;
    }

    public EC2Machine getBestMachine(int workload){
        ArrayList<EC2Machine> flaggedMachines = new ArrayList<EC2Machine>();
        EC2Machine best = null;
        int minimumHeavyness = -1;
        int currentHeavyness = -1;
        //TO DO: Change getServerRequestCount to getServerWorkload
        for(Map.Entry<String, EC2Machine> entry : machines.entrySet()){
            EC2Machine ec2 = entry.getValue();
            if(best == null || ((currentHeavyness = ec2.getAbsoluteWorkload()) < minimumHeavyness && !ec2.getTerminateFlag())){
                if(best != null)
                    minimumHeavyness = currentHeavyness;
                else
                    minimumHeavyness = ec2.getAbsoluteWorkload();
                best = ec2;

                //minimumHeavyness = currentHeavyness;
            }
            if(ec2.getTerminateFlag()){
                flaggedMachines.add(ec2);
            }
        }
        EC2Machine newBest = null;
        //check if best workload is not over the threshold. If it is, launch a new machine.
        if(minimumHeavyness >= WORKLOAD_COEFFICIENT*WORKLOAD_THRESHOLD){
            //instead of booting a new machine, first check if a flagged machine should be resurrected.
            //newBest = this.createNewMachine(this.IMAGE_ID, this.INSTANCE_TYPE, this.KEY_NAME, this.SECURITY_GROUP, this.REGION);
            for(EC2Machine flagged : flaggedMachines) {
                if(flagged.getAbsoluteWorkload() < WORKLOAD_COEFFICIENT*WORKLOAD_THRESHOLD) {
                    flagged.setTerminateFlag(false);
                    newBest = flagged;
                    break;
                }
            }
            if(newBest == null){
                TimerTask t = new CreateNewMachineTask(this);
                Timer timer = new Timer(true);
                timer.schedule(t, 0);
            }
        }
        if(workload >= 18){
            //since request is so big, we can allocate a machine for it and wait a lil for it to boot.
            best = this.createNewMachine(this.IMAGE_ID, INSTANCE_TYPE, KEY_NAME, SECURITY_GROUP, REGION);
        }
        System.out.println("Best EC2 Workload: " + minimumHeavyness);
        return newBest == null ? best : newBest;

    }


    public void terminateAllMachines(){
        for(Map.Entry<String, EC2Machine> entry : this.machines.entrySet()){
            //this.terminateMachine(entry.getKey());
            entry.getValue().setTerminateFlag(true);
        }
    }

    public void terminateMachine(String instanceid){
        if(this.machines.size() > this.minimumMachines || this.machines.get(instanceid).getFailedProofsOfLife() >= MAXIMUM_FAILED_HEALTH_CHECKS) {
            EC2Machine toTerminate = this.machines.get(instanceid);
            if(toTerminate.getAbsoluteWorkload() != 0)
                return;
            while (!toTerminate.terminateMachine()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                    System.out.println(ie.getMessage());
                }
            }
            //System.out.println("Machine " + instanceid + " terminated successfully.");
            this.machines.remove(instanceid);
            System.out.println("EC2AutoScaler Scaling Down: Current System Size: " + this.machines.size());
        }else{
            System.out.println("Can't kill machine " + instanceid + " because it's the last machine.");
            this.machines.get(instanceid).setTerminateFlag(false);
            this.machines.get(instanceid).setIdleTime(0);
        }
    }

    public int getMinimumMachines(){ return this.minimumMachines; }
    public void setMinimumMachines(int min) { this.minimumMachines = min;}
    public int getMaximumMachines(){ return this.maximumMachines;}
    public void setMaximumMachines(int max){ this.maximumMachines = max;}


    public void checkSystemState(){
        System.out.println("EC2AutoScaler: System Check");
        EC2Machine leastOccupied = null;
        ArrayList<EC2Machine> machinesFlagged = new ArrayList<EC2Machine>();
        int leastWorkload = -1;
        int currentWorkload = 0;
        Iterator<String> iter = this.machines.keySet().iterator();
        //for(Map.Entry<String, EC2Machine> entry : this.machines.entrySet()) {
        //for(int i = 0; i < this.machines.size(); i++){
        if(this.getFutureMachineCount() < this.getMinimumMachines()){
            System.out.println("EC2AutoScaler: Machines Going Online: " + this.getMachinesGoingOnline() + ". Minimum Machines: " + this.getMinimumMachines());
            System.out.println("EC2AutoScaler: Less than minimum machines! Spinning a new one up.");
            this.setMachinesGoingOnline(this.getMachinesGoingOnline()+1);
            TimerTask t = new CreateNewMachineTask(this);
            Timer timer = new Timer(true);
            timer.schedule(t, 0);
            //return;//this.createNewMachine(this.IMAGE_ID, this.INSTANCE_TYPE, this.KEY_NAME, this.SECURITY_GROUP, this.REGION);
        }

        //while(iter.hasNext()){
        for(Map.Entry<String, EC2Machine> entry : this.machines.entrySet()){
            EC2Machine ec2 = entry.getValue();//this.machines.get(iter.next());
            if (!ec2.getProofOfLife()) {
                ec2.incrementFailedProofsOfLife();
                System.out.println("Failed health check on " + ec2.getInstanceId() + ". Current failed proofs of life: " + ec2.getFailedProofsOfLife());
                if (ec2.getFailedProofsOfLife() >= MAXIMUM_FAILED_HEALTH_CHECKS) {
                    //ec2.setTerminateFlag(true);
                    //set flag or instant kill?
                    System.out.println("EC2AutoScaler: Machine " + ec2.getInstanceId() + " failed " + MAXIMUM_FAILED_HEALTH_CHECKS + " health checks in a row");
                    this.terminateMachine(ec2.getInstanceId());
                }
                continue;
            }
            if (leastOccupied == null || (currentWorkload = ec2.getAbsoluteWorkload()) < leastWorkload) {
                if(leastOccupied != null)
                    leastWorkload = currentWorkload;
                else
                    leastWorkload = ec2.getAbsoluteWorkload();

                leastOccupied = ec2;

            }
            if (ec2.getTerminateFlag()) {
                if (ec2.getTimeSinceLastFlag() >= MACHINE_KEEP_ALIVE * 1000) {
                    System.out.println("EC2AutoScaler: Shutting down instance " + ec2.getInstanceId());
                    this.terminateMachine(ec2.getInstanceId());
                }
                else {
                    ec2.setTimeSinceLastFlag(ec2.getTimeSinceLastFlag() + this.SYSTEM_CHECK_TIME * 1000);
                    System.out.println("EC2AutoScaler: Time since last flag for instance " + ec2.getInstanceId() + " : " + ec2.getTimeSinceLastFlag());
                }
            }
            if (ec2.getIdleTime() >= MAX_IDLE_TIME)
                ec2.setTerminateFlag(true);
            //perform health check time

        }
        System.out.println("Least Workload: " + leastWorkload);
        //TODO: Fix bug where a new machine is still launched even though one just came up online.
        synchronized(this) {
            if (leastWorkload >= WORKLOAD_COEFFICIENT * WORKLOAD_THRESHOLD && this.getFutureMachineCount() <= this.machines.size()) {
                this.setMachinesGoingOnline(this.getMachinesGoingOnline() + 1);
                System.out.println("System is overloaded. Booting a new machine up preemptively");
                TimerTask t = new CreateNewMachinePreemptivelyTask(this);
                Timer timer = new Timer(true);
                timer.schedule(t, 0);
            }
        }

        //System.out.println("FutureMachineCount: " + this.getFutureMachineCount());
        //System.out.println("MachinesGoingOnline: " + this.machinesGoingOnline);


        //TODO: ADD REMAINING LOGIC FOR CHECKS
    }

    public int getMachineCount(){
        return this.machines.size();
    }

    public int getFutureMachineCount(){
        return this.machines.size() + machinesGoingOnline;
    }

    public void setMachinesGoingOnline(int m){
        this.machinesGoingOnline = m;
    }

    public int getMachinesGoingOnline(){
        return this.machinesGoingOnline;
    }

    public void setSystemCheckTime(int check){
        this.SYSTEM_CHECK_TIME = check;
    }

    public int getSystemCheckTime(){
        return this.SYSTEM_CHECK_TIME;
    }

    class CreateNewMachineTask extends TimerTask {
        EC2AutoScaler scaler;
        CreateNewMachineTask(EC2AutoScaler s){ scaler = s;}
        @Override
        public void run(){
            scaler.createNewMachine(scaler.IMAGE_ID, scaler.INSTANCE_TYPE, scaler.KEY_NAME, scaler.SECURITY_GROUP, scaler.REGION);
            scaler.setMachinesGoingOnline(scaler.getMachinesGoingOnline()-1);
        }
    }
    class CreateNewMachinePreemptivelyTask extends TimerTask{
        EC2AutoScaler scaler;
        CreateNewMachinePreemptivelyTask(EC2AutoScaler s){ scaler = s;}
        @Override
        public void run(){
            scaler.createNewMachine(scaler.IMAGE_ID, scaler.INSTANCE_TYPE, scaler.KEY_NAME, scaler.SECURITY_GROUP, scaler.REGION);
            scaler.setMachinesGoingOnline(scaler.getMachinesGoingOnline()-1);
            //scaler.canBootPreemptiveMachine.compareAndSet(false, true);
        }
    }
}
