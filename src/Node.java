
import java.util.ArrayList;
import java.util.Vector;
public class Node {
    private int id;
    private int usedNo;
    private Vector<Double> req;
    private ArrayList<Integer> vSetLst;
    private ArrayList<ArrayList<Integer>> vSetComb;
    
    public Node()
    {
    	this.id =-1;
    	this.usedNo = -1;
    	this.req= new Vector<>(3);
    	for (int i=0;i<3;i++)
        {
        	double temp=0.0;
        	req.addElement(temp);
        }
    	this.vSetComb = new ArrayList<>();
    }
    public Node(int _id)
    {
    	this.id =_id;
    	this.usedNo = -1;
    	this.req= new Vector<>(3);
    	for (int i=0;i<3;i++)
        {
        	double temp=0.0;
        	req.addElement(temp);
        }
    	this.vSetComb = new ArrayList<>();
    }
    public Node(int _id, Vector<Double> _req, int _usedNo, ArrayList<Integer> _vSetLst)
    {
    	this.id=_id;
    	this.req= new Vector<>(3);
    	for (int i=0;i<3;i++)
        {
        	req.addElement(_req.get(i));
        }
    	this.usedNo = _usedNo;
    	vSetLst = new ArrayList<>();
    	for (int i=0;i<_vSetLst.size();i++)
    		vSetLst.add(_vSetLst.get(i));
    	this.vSetComb = new ArrayList<>();
    }
    public boolean CompareTo(Node n)
    {
    	if(this.id==n.id)
    		return true;
    	else
    		return false;
    }
    public int getid()
    {
    	return id;
    }
    public Vector<Double> getReq()
    {
    	return req;
    }
    public int getusedNo()
    {
    	return usedNo;
    }
    public ArrayList<Integer> getvSetLst()
    {
    	return vSetLst;
    }
    public ArrayList<ArrayList<Integer>> getvSetComb()
    {
    	return vSetComb;
    }
    public void setid(int _id)
    {
    	this.id=_id;
    }
    public void setReq(Vector<Double> _req)
    {
    	this.req= new Vector<>(3);
    	for (int i=0;i<3;i++)
        {
        	req.addElement(_req.get(i));
        }
    }
    public void setusedNo(int _usedNo)
    {
    	this.usedNo=_usedNo;
    }
    public void setvSetLst(ArrayList<Integer> _vSetLst)
    {
    	vSetLst = new ArrayList<>();
    	for (int i=0;i<_vSetLst.size();i++)
    		vSetLst.add(_vSetLst.get(i));
    }
    public void setvSetComb(ArrayList<ArrayList<Integer>> _vSetCom)
    {
    	this.vSetComb = new ArrayList<>();
    	this.vSetComb = _vSetCom;
    }
}