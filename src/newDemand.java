import java.util.ArrayList;
import java.util.Collections;


public class newDemand {
	//source, destination,bandwidth,arrival time,set of function (number of function)
    private int id;//id of demand
    private int noF;//number of Functions
    private double bwS;//bandwidth
    private int source;
    private int destination;
    private double arrivalTime;
    private double processTime;
    private double rate;//rate requirement for each demand
    private ArrayList<Integer> arrF;//set of functions
    
    public newDemand()
    {
    	this.id =-1;
    }
    
    public newDemand(int Id, int noV, double currentTime, ArrayList<Function> F,int MinServer,int MaxServer){
 	   this.id= Id;
 	   this.source = UtilizeFunction.randInt(MinServer, MaxServer);
 	   while (true)
        {
        	int desTemp= UtilizeFunction.randInt(MinServer, MaxServer);
        	if(desTemp !=source)
        	{
        		this.destination = desTemp;
        		break;
        	}
        }
 	   Integer[] intArray = new Integer[] { 50,100,150,200 };
        this.bwS = UtilizeFunction.randomDouble(intArray);
 	   this.arrivalTime = currentTime;
 	   this.processTime = UtilizeFunction.randDouble(0.0,50.0);
 	   this.noF = UtilizeFunction.randInt(2, F.size());
 	   this.rate = UtilizeFunction.randDouble(0.0,5.0);
 	   this.arrF = new ArrayList<Integer>();
 	   ArrayList<Integer> idFArr= new ArrayList<Integer>();
    		for (int i=0;i<F.size();i++)
    			idFArr.add(F.get(i).getId());
 	   
 	   for(int i= 0;i<noF;i++)
 	   {
 		   boolean flag= false;
 	    	while (!flag)
 	    	{
 	    		Collections.shuffle(idFArr);
 	    		int idF= idFArr.get(0);
 	    		if(!arrF.contains(idF))
 	    		{
 	    			arrF.add(idF);
 	    			flag=true;
 	    		}
 	    	}
 	   }
    }
    //random
   public newDemand(int Id, int noV, double currentTime, ArrayList<Function> F){
	   this.id= Id;
	   this.source = UtilizeFunction.randInt(1, noV);
	   while (true)
       {
       	int desTemp= UtilizeFunction.randInt(1, noV);
       	if(desTemp !=source)
       	{
       		this.destination = desTemp;
       		break;
       	}
       }
	   Integer[] intArray = new Integer[] { 100,150,200,300,400,500 };
       this.bwS = UtilizeFunction.randomDouble(intArray);
	   this.arrivalTime = currentTime;
	   this.processTime = UtilizeFunction.randDouble(0.0,50.0);
	   this.noF = UtilizeFunction.randInt(1, F.size());
	   this.rate = UtilizeFunction.randDouble(0.0,5.0);
	   this.arrF = new ArrayList<Integer>();
	   ArrayList<Integer> idFArr= new ArrayList<Integer>();
   		for (int i=0;i<F.size();i++)
   			idFArr.add(F.get(i).getId());
	   
	   for(int i= 0;i<noF;i++)
	   {
		   boolean flag= false;
	    	while (!flag)
	    	{
	    		Collections.shuffle(idFArr);
	    		int idF= idFArr.get(0);
	    		if(!arrF.contains(idF))
	    		{
	    			arrF.add(idF);
	    			flag=true;
	    		}
	    	}
	   }
   }

 public newDemand(int Id, int Source,int Destination,double ArrivalTime,double ProcessTime,double Bw,double Rate,ArrayList<Integer> arrayF)
 {    	//truong hop khong random
	this.id=Id;	
 	this.source=Source;
 	this.rate = Rate;
 	this.destination = Destination;
 	this.arrivalTime = ArrivalTime;
 	this.processTime = ProcessTime;
 	this.bwS= Bw;
 	this.noF = arrayF.size();
 	this.arrF = new ArrayList<Integer>();
	for (int i=0;i<this.noF;i++)
		this.arrF.add(arrayF.get(i));
    
}

    // id of Service
    public int getId() { return id; }
    // return Source
    public int getSrc() { return source; }
 // return Destination
    public int getDest() { return destination; }
 // return arrival Time
    public double getArrivalTime() { return arrivalTime; }
    public double getProcessTime() { return processTime; }
    // number of functions in service
    public int getNoF() { return noF; }
    // return bandwidth of service;
    public double getBw() { return this.bwS; }
    //return array of Function in service;
    public ArrayList<Integer> getFunctions() {return this.arrF;}
    public double getRate(){return this.rate;}
    public int getOrderFunction(int id)
    {
    	int temp =0;
    	if (id ==0)
    		return 0;
    	for (int x= 0; x<this.arrF.size(); x++)
    	{
    		if (arrF.get(x)==id)
    		{
    			temp=x+1;
    			break;
    			
    		}
    	}
    	return temp;
    }

    public static void main(String[] args) {
    	
    }

}