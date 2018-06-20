import java.util.ArrayList;
import java.util.Collections;


public class oldDemand {
	//source, destination,bandwidth,arrival time,process time,set of function (number of function),v_sol,f_sol
    private int id;//id of demand
    private double bw;//bandwidth
    private int source;
    private int destination;
    private ArrayList<Integer> setFunction;
    private ArrayList<Integer> v_solution;
	private ArrayList<Integer> f_solution;
    private double arrivalTime;
    private double processtime;
    private double rate;
 public oldDemand()
 {	
	 this.id=-1;
 }
 public oldDemand(int Id, ArrayList<Function> F,int noVirtualNode,double currentTime,int MinServer,int MaxServer) {	
		//Random
	        this.id=Id;
	        this.source=UtilizeFunction.randInt(MinServer, MaxServer);
	        while (true)
	        {
	        	int desTemp= UtilizeFunction.randInt(MinServer, MaxServer);
	        	if(desTemp !=source)
	        	{
	        		this.destination = desTemp;
	        		break;
	        	}
	        }
	        this.arrivalTime = UtilizeFunction.randDouble(0.0,currentTime);
	        this.processtime = UtilizeFunction.randDouble(currentTime - arrivalTime,50.0);
	        this.rate = UtilizeFunction.randDouble(0.0,5.0);
	        Integer[] intArray = new Integer[] { 50,100,150,200 };
	        this.bw = UtilizeFunction.randomDouble(intArray);
	        int lengPath= UtilizeFunction.randInt(2, 15);
	        this.v_solution = new ArrayList<Integer>();
	    	this.f_solution=new ArrayList<Integer>();
	    	this.setFunction=new ArrayList<Integer>();
	    	ArrayList<Integer> idFArr= new ArrayList<Integer>();
	    	for (int i=0;i<F.size();i++)
	    		idFArr.add(F.get(i).getId());
	    	idFArr.add(0);
	        //create array for path of demand
	    	v_solution.add(source);
	    	Collections.shuffle(idFArr);
			f_solution.add(idFArr.get(0));
	        for(int i=1;i<lengPath-1;i++)
	        {
	        	boolean flag=false;
	        	while (!flag)
	        	{
	        		int temp=UtilizeFunction.randInt(1, noVirtualNode);
	        		if(i==0 || v_solution.get(i-1)!=temp)
	        			if((i==0)|| i==1 || v_solution.get(i-2)!=temp)
	        			{
	        				v_solution.add(temp);
	        				flag=true;
	        			}
	        	}
	        	flag=false;
	        	while (!flag)
	        	{
	        		Collections.shuffle(idFArr);
	        		int temp = idFArr.get(0);
	        		boolean chua = f_solution.contains(temp);
	        		if(temp==0 || !chua)
	        		{
	        			f_solution.add(temp);
	        			flag=true;
	        		}        		
	        	}
	        }
	        v_solution.add(destination);
	    	while (true)
	    	{
	    		Collections.shuffle(idFArr);
	    		int temp = idFArr.get(0);
	    		boolean chua = f_solution.contains(temp);
	    		if(temp==0 || !chua)
	    		{
	    			f_solution.add(temp);
	    			break;
	    		}        		
	    	}
	        for(int i=0;i<f_solution.size();i++)
	        	if(f_solution.get(i)>0)
	        		setFunction.add(f_solution.get(i));
	        
	}
 
public oldDemand(int Id, ArrayList<Function> F,int noVirtualNode,double currentTime) {	
	//Random
        this.id=Id;
        this.source=UtilizeFunction.randInt(1, noVirtualNode);
        while (true)
        {
        	int desTemp= UtilizeFunction.randInt(1, noVirtualNode);
        	if(desTemp !=source)
        	{
        		this.destination = desTemp;
        		break;
        	}
        }
        this.arrivalTime = UtilizeFunction.randDouble(0.0,currentTime);
        this.processtime = UtilizeFunction.randDouble(currentTime - arrivalTime,50.0);
        this.rate = UtilizeFunction.randDouble(0.0,5.0);
        Integer[] intArray = new Integer[] { 50,100,150,200 };
        this.bw = UtilizeFunction.randomDouble(intArray);
        int lengPath= UtilizeFunction.randInt(2, 15);
        this.v_solution = new ArrayList<Integer>();
    	this.f_solution=new ArrayList<Integer>();
    	this.setFunction=new ArrayList<Integer>();
    	ArrayList<Integer> idFArr= new ArrayList<Integer>();
    	for (int i=0;i<F.size();i++)
    		idFArr.add(F.get(i).getId());
    	idFArr.add(0);
        //create array for path of demand
    	v_solution.add(source);
    	Collections.shuffle(idFArr);
		f_solution.add(idFArr.get(0));
        for(int i=1;i<lengPath-1;i++)
        {
        	boolean flag=false;
        	while (!flag)
        	{
        		int temp=UtilizeFunction.randInt(1, noVirtualNode);
        		if(i==0 || v_solution.get(i-1)!=temp)
        			if((i==0)|| i==1 || v_solution.get(i-2)!=temp)
        			{
        				v_solution.add(temp);
        				flag=true;
        			}
        	}
        	flag=false;
        	while (!flag)
        	{
        		Collections.shuffle(idFArr);
        		int temp = idFArr.get(0);
        		boolean chua = f_solution.contains(temp);
        		if(temp==0 || !chua)
        		{
        			f_solution.add(temp);
        			flag=true;
        		}        		
        	}
        }
        v_solution.add(destination);
    	while (true)
    	{
    		Collections.shuffle(idFArr);
    		int temp = idFArr.get(0);
    		boolean chua = f_solution.contains(temp);
    		if(temp==0 || !chua)
    		{
    			f_solution.add(temp);
    			break;
    		}        		
    	}
        for(int i=0;i<f_solution.size();i++)
        	if(f_solution.get(i)>0)
        		setFunction.add(f_solution.get(i));
        
}
   
    public oldDemand(int IdS,int Source,int Destination,double ArrivalTime,double ProcessTime,double BwS, double Rate,ArrayList<Integer> SetFunction,ArrayList<Integer> v_sol, ArrayList<Integer> f_sol) {
    	this.id = IdS;
    	this.bw = BwS;
    	this.source=Source;
    	this.destination = Destination;
    	this.arrivalTime = ArrivalTime;
    	this.processtime = ProcessTime;
    	this.rate = Rate;
    	 this.v_solution = new ArrayList<Integer>();
     	this.f_solution=new ArrayList<Integer>();
     	this.setFunction=new ArrayList<Integer>();
    	for(int i=0;i<SetFunction.size();i++)
    		setFunction.add(SetFunction.get(i));
    	for(int i=0;i<v_sol.size();i++)
    		v_solution.add(v_sol.get(i));
    	for(int i=0;i<f_sol.size();i++)
    		f_solution.add(f_sol.get(i));
        
    }
    public int GetID() { return id; }
    public int GetSrc() { return source; }
    public int GetDest() { return destination; }
    public double GetArrivalTime() { return arrivalTime; }
    public double GetProcessTime() { return processtime; }
    public double GetBandwidth() { return this.bw; }
    public double GetRate(){return this.rate;}
    public boolean updateVSol(ArrayList<Integer> v_sol)
    {
    	v_solution =null;
    	v_solution =new ArrayList<Integer>();
    	for(int i=0;i<v_sol.size();i++)
    		v_solution.add(v_sol.get(i));
    	return true;
    }
    public boolean updateFSol(ArrayList<Integer> f_sol)
    {
    	f_solution =null;
    	f_solution =new ArrayList<Integer>();
    	for(int i=0;i<f_sol.size();i++)
    		f_solution.add(f_sol.get(i));
    	return true;
    }
    public ArrayList<Integer> Get_v_sol(){return v_solution;}
    public ArrayList<Integer> Get_f_sol(){return f_solution;}
    public ArrayList<Integer> GetSetFunc(){return setFunction;}
    public static void main(String[] args) {
    }

}