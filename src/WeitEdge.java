
public class WeitEdge {
    private int o;
    private int d;
    private double w;
    
    public WeitEdge()
    {
    	this.o =-1;
    	this.d = -1;
    	this.w=0;
    }
    public WeitEdge(int _o, int _d,double _w)
    {
    	this.o=_o;
    	this.d = _d;
    	this.w = _w;
    }
    public boolean CompareTo(WeitEdge l)
    {
    	if(this.o==l.o && this.d==l.d)
    		return true;
    	else
    		return false;
    }
    public int getO()
    {
    	return o;
    }
    public int getD()
    {
    	return d;
    }
    public double getW()
    {
    	return w;
    }
    public void setO(int _o)
    {
    	this.o=_o;
    }
    public void setD(int _d)
    {
    	this.d=_d;
    }
    public void setW(double _w)
    {
    	this.w= _w;
    }
}