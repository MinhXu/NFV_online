import java.util.ArrayList;
import java.util.Collections;


public class nDemand {
	//source, destination,bandwidth,arrival time,set of function (number of function)
    private int id;//id of demand
    private int noF;//number of Functions
    private double bwS;//bandwidth //rate
    private int source;
    private int destination;
    private ArrayList<Integer> arrF;//set of functions
    private double arrivalTime;
    private double processTime;
    
    public nDemand()
    {
    	this.id =-1;
    }
    
    public nDemand(int Id, int noV, ArrayList<nFunction> F,int MinServer,int MaxServer){
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
 	   this.noF = UtilizeFunction.randInt(2, F.size());
 	   this.arrF = new ArrayList<Integer>();
 	   ArrayList<Integer> idFArr= new ArrayList<Integer>();
    		for (int i=0;i<F.size();i++)
    			idFArr.add(F.get(i).id());
 	   
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
    public nDemand(int Id, int noV,  ArrayList<nFunction> F,int _noF,double _arrvingTime ,double _processTime){
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
 	   Integer[] intArray = new Integer[] {50,100,150,200,200};
        this.bwS = 0.1;
 	   this.noF = _noF;
 	   this.arrF = new ArrayList<Integer>();
 	   ArrayList<Integer> idFArr= new ArrayList<Integer>();
    		for (int i=0;i<F.size();i++)
    			idFArr.add(F.get(i).id());
 	   
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
 	   this.processTime=_processTime;
 	   this.arrivalTime= _arrvingTime;
    }
    
   public nDemand(int Id, int noV,  ArrayList<nFunction> F){
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
	   Integer[] intArray = new Integer[] {50,100,150,200,200};
       this.bwS = UtilizeFunction.randomDouble(intArray);
	   this.noF = UtilizeFunction.randInt(3, F.size());
	   this.arrF = new ArrayList<Integer>();
	   ArrayList<Integer> idFArr= new ArrayList<Integer>();
   		for (int i=0;i<F.size();i++)
   			idFArr.add(F.get(i).id());
	   
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

 public nDemand(int Id, int Source,int Destination,double Bw,ArrayList<Integer> arrayF)
 {    	//truong hop khong random
	this.id=Id;	
 	this.source=Source;
 	this.destination = Destination;
 	this.bwS= Bw;
 	this.noF = arrayF.size();
 	this.arrF = new ArrayList<Integer>();
	for (int i=0;i<arrayF.size();i++)
		this.arrF.add(arrayF.get(i));
    
}
 public double getArrivalTime()
 {
	 return arrivalTime;
 }
 public double getProcessTime()
 {
	 return processTime;
 }
 public boolean setArrivalTime(double _arrivalTime)
 {
	 arrivalTime=_arrivalTime;
	 return true;
 }
 public boolean setProcessTime(double _processTime)
 {
	 processTime=_processTime;
	 return true;
 }

    // id of Service
    public int getId() { return id; }
    // return Source
    public int getSrc() { return source; }
 // return Destination
    public int getDest() { return destination; }
    // number of functions in service
    public int getNoF() { return noF; }
    // return bandwidth of service;
    public double getBw() { return this.bwS; }
    //return array of Function in service;
    public ArrayList<Integer> getFunctions() {return this.arrF;}
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