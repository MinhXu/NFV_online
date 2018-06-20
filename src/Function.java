import java.util.Vector;


public class Function {
	private int id;
    private Vector<Double> lamda= new Vector<>(3);
    
    public Function()
    {
    	this.id =-1;
    }
    public Function(int id) {//khoi tao random function
        if ( id <=0 ) throw new RuntimeException("Number of vertices must be nonnegative");
        this.id = id; 
        for (int i=0;i<3;i++)
        {
        	double temp= UtilizeFunction.randDouble(50);
        	lamda.addElement(temp);
        } 
    }
    public Function(Vector<Double> hso, int id) {//khoi tao random function
        if ( id <=0 ) throw new RuntimeException("Number of vertices must be nonnegative");
        this.id = id; 
        for (int i=0;i<3;i++)
        {
        	double temp= UtilizeFunction.randDouble(1,hso.get(i));
        	lamda.addElement(temp);
        } 
    }

    public Function(int id, double bw) {//gan id va bw cho 1 function
        this(id); 
    }
    public Function(int _id, Vector<Double> _lamda) {//gan id va bw cho 1 function
        id=_id;
        for (int i=0;i<3;i++)
        {
        	lamda.addElement(_lamda.get(i));
        }       
    }

    // number of vertices and edges
    public int getId() { return id; }
    public Vector<Double> getLamda()
    {
    	return lamda;
    }


    // test client
    public static void main(String[] args) {
    }

}