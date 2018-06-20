import java.util.ArrayList;


public class nOldDemand {
	//source, destination,bandwidth,arrival time,process time,set of function (number of function),v_sol,f_sol
    private int id;//id of demand
    private double bw;//bandwidth
    private ArrayList<Pair> funLoc;
	//private ArrayList<ArrayList<Integer>> path;
    private ArrayList<Integer> path;
    private double arrivalTime;
    private double processtime;
 public nOldDemand()
 {	
	 this.id=-1;
 }

    public nOldDemand(int IdS,double ArrivalTime,double ProcessTime,double BwS,ArrayList<Integer> Path,ArrayList<Pair> FunLoc) {
    	this.id = IdS;
    	this.bw = BwS;
    	this.arrivalTime = ArrivalTime;
    	this.processtime = ProcessTime;
    	 this.path = new ArrayList<>();
     	this.funLoc=new ArrayList<Pair>();
     	this.path = Path;
     	this.funLoc = FunLoc;
        
    }
    public int GetID() { return id; }
    public double GetArrivalTime() { return arrivalTime; }
    public double GetProcessTime() { return processtime; }
    public double GetBandwidth() { return this.bw; }
    public ArrayList<Integer> Get_path(){return this.path;}
    public ArrayList<Pair> Get_funLoc(){return this.funLoc;}
    public static void main(String[] args) {
    }

}