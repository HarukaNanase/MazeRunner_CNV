import java.util.*;

public class EC2AutoScaler {

    private HashMap<String, EC2Machine> machines;
    private int minimumMachines = 1;
    private int maximumMachines = 20;
    private int MAXIMUM_HEAVYNESS = 85;
    private String REGION = "us-east-1";
    private String IMAGE_ID = "ami-092e2e3869c41af60";
    private String INSTANCE_TYPE = "t2.micro";
    private String KEY_NAME = "CNV_AWS";
    private String SECURITY_GROUP = "CNV-HTTP-SSH";
    private int WORKLOAD_THRESHOLD = 90;
    private int MACHINE_KEEP_ALIVE = 200;
    private int SYSTEM_CHECK_TIME = 10;
    private int machinesGoingOnline = 0;

    public EC2AutoScaler(){

        this.machines = new HashMap<String, EC2Machine>();
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

    public HashMap<String, EC2Machine> getMachines(){
        return this.machines;
    }

    public EC2Machine getBestMachine(){
        ArrayList<EC2Machine> flaggedMachines = new ArrayList<EC2Machine>();
        EC2Machine best = null;
        int minimumHeavyness = -1;
        int currentHeavyness = -1;
        //TO DO: Change getServerRequestCount to getServerWorkload
        for(Map.Entry<String, EC2Machine> entry : machines.entrySet()){
            EC2Machine ec2 = entry.getValue();
            if(best == null || ((currentHeavyness = ec2.getServerRequestCount()) < minimumHeavyness && !ec2.getTerminateFlag())){
                best = ec2;
                minimumHeavyness = currentHeavyness;
            }
            if(ec2.getTerminateFlag()){
                flaggedMachines.add(ec2);
            }
        }
        EC2Machine newBest = null;
        //check if best workload is not over the threshold. If it is, launch a new machine.
        if(minimumHeavyness > MAXIMUM_HEAVYNESS){
            //instead of booting a new machine, first check if a flagged machine should be resurrected.
            //newBest = this.createNewMachine(this.IMAGE_ID, this.INSTANCE_TYPE, this.KEY_NAME, this.SECURITY_GROUP, this.REGION);
            for(EC2Machine flagged : flaggedMachines)
                newBest = flagged;
        }
        return newBest == null ? best : newBest;

    }


    public void terminateAllMachines(){
        for(Map.Entry<String, EC2Machine> entry : this.machines.entrySet()){
            //this.terminateMachine(entry.getKey());
            entry.getValue().setTerminateFlag(true);
        }
    }

    public void terminateMachine(String instanceid){
        if(this.machines.size() > this.minimumMachines) {
            EC2Machine toTerminate = this.machines.get(instanceid);
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
        }
    }

    public int getMinimumMachines(){ return this.minimumMachines; }
    public void setMinimumMachines(int min) { this.minimumMachines = min;}
    public int getMaximumMachines(){ return this.maximumMachines;}
    public void setMaximumMachines(int max){ this.maximumMachines = max;}

    public void checkSystemState(){
        EC2Machine leastOccupied = null;
        ArrayList<EC2Machine> machinesFlagged = new ArrayList<EC2Machine>();
        int leastWorkload = -1;
        int currentWorkload = -1;
        for(Map.Entry<String, EC2Machine> entry : this.machines.entrySet()){
            EC2Machine ec2 = entry.getValue();
            if(leastOccupied == null || (currentWorkload = ec2.getMachineWorkLoad()) < leastWorkload){
                leastOccupied = ec2;
                leastWorkload = currentWorkload;
            }
            if(ec2.getTerminateFlag()){
                if(ec2.getTimeSinceLastFlag() >= MACHINE_KEEP_ALIVE*1000)
                    this.terminateMachine(ec2.getInstanceId());
                else{
                    ec2.setTimeSinceLastFlag(ec2.getTimeSinceLastFlag() + this.SYSTEM_CHECK_TIME*1000);
                    System.out.println("Time since flag: " + ec2.getTimeSinceLastFlag());
                }
            }
        }

        if(leastWorkload >= WORKLOAD_THRESHOLD){
            System.out.println("System is overloaded. Booting a new machine up preemptively");
            this.createNewMachine(this.IMAGE_ID, this.INSTANCE_TYPE, this.KEY_NAME, this.SECURITY_GROUP, this.REGION);
        }

        System.out.println("FutureMachineCount: " + this.getFutureMachineCount());
        System.out.println("MachinesGoingOnline: " + this.machinesGoingOnline);
        if(this.getFutureMachineCount() < this.getMinimumMachines()){
            System.out.println("EC2AutoScaler: Less than minimum machines! Spinning a new one up.");
            TimerTask t = new CreateNewMachineTask(this);
            Timer timer = new Timer(true);
            timer.schedule(t, 0);
            //this.createNewMachine(this.IMAGE_ID, this.INSTANCE_TYPE, this.KEY_NAME, this.SECURITY_GROUP, this.REGION);
        }

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
            scaler.setMachinesGoingOnline(scaler.getMachinesGoingOnline()+1);
            scaler.createNewMachine(scaler.IMAGE_ID, scaler.INSTANCE_TYPE, scaler.KEY_NAME, scaler.SECURITY_GROUP, scaler.REGION);
            scaler.setMachinesGoingOnline(scaler.getMachinesGoingOnline()-1);
        }
    }
}
