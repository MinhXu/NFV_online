import java.util.ArrayList;


public class wLink {
    private int start;
    private int end;
    private double w;
    private ArrayList<Integer> CnnSetLst;
    private ArrayList<ArrayList<Integer>> cnnSetComb;
    
    public wLink()
    {
    	this.start =-1;
    	this.end = -1;
    	this.w=0.0;
    	this.CnnSetLst = new ArrayList<>();
    	this.cnnSetComb = new ArrayList<>();
    }
    public wLink(int _start, int _end, double _w, ArrayList<Integer> _CnnSetLst)
    {
    	this.start=_start;
    	this.end = _end;
    	this.w=_w;
    	this.CnnSetLst = _CnnSetLst;
    	this.cnnSetComb = new ArrayList<>();
    }
    public boolean CompareTo(wLink l)
    {
    	if(this.start==l.start && this.end==l.end )
    		return true;
    	else
    		return false;
    }
    public int getStart()
    {
    	return start;
    }
    public int getEnd()
    {
    	return end;
    }
    public double getWeight()
    {
    	return w;
    }
    public ArrayList<Integer> getCnnSet()
    {
    	return CnnSetLst;
    }
    public ArrayList<ArrayList<Integer>> getCnnSetComb()
    {
    	return cnnSetComb;
    }
    public void setWeight(double _w)
    {
    	this.w= _w;
    }
    public void setStart(int _start)
    {
    	this.start=_start;
    }
    public void setEnd(int _end)
    {
    	this.end=_end;
    }
    public void setCnnSet(ArrayList<Integer> cnnSet)
    {
    	this.CnnSetLst = cnnSet;
    }
    public void setCnnSetComb(ArrayList<ArrayList<Integer>> cnnSetCom)
    {
    	this.cnnSetComb = cnnSetCom;
    }
}