import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EC2AutoScaler {

    private HashMap<String, EC2Machine> machines;
    private int minimumMachines = 1;
    private int maximumMachines = 5;

    public EC2AutoScaler(){
            this.machines = new HashMap<String, EC2Machine>();
    }


    public EC2Machine createNewMachine(){
        if(this.machines.size() < this.maximumMachines) {
            EC2Machine machine = new EC2Machine();
            try {
                machine.launchMachine();
                this.machines.put(machine.getInstanceId(), machine);
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
        EC2Machine best = null;
        int minimumHeavyness = -1;
        int currentHeavyness = -2;
        //TO DO: Change getServerRequestCount to getServerWorkload
        for(Map.Entry<String, EC2Machine> entry : machines.entrySet()){
            EC2Machine ec2 = entry.getValue();
            if(best == null || (currentHeavyness = ec2.getServerRequestCount()) < minimumHeavyness){
                best = ec2;
                minimumHeavyness = currentHeavyness;
            }
        }

        return best;

    }

    public void terminateMachine(String instanceid){
        if(this.machines.size() > this.minimumMachines) {
            EC2Machine toTerminate = this.machines.get(instanceid);
            while (!toTerminate.terminateMachine()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
            System.out.println("Machine " + instanceid + " terminated successfully.");
            this.machines.remove(instanceid);
        }
    }

    public int getMinimumMachines(){ return this.minimumMachines; }
    public void setMinimumMachines(int min) { this.minimumMachines = min;}
    public int getMaximumMachines(){ return this.maximumMachines;}
    public void setMaximumMachines(int max){ this.maximumMachines = max;}

}
