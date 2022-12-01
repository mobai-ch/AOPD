package NetWork.Element;

import java.util.ArrayList;
import java.util.List;

public class Path {
    public String nodes = "";
    public double travelTime;

    public void addNode(int node){
        String n = Integer.toString(node);
        nodes += (n + ";");
    }

    public List<Integer> getNodes(){
        String s = nodes.substring(0, nodes.length() - 1);
        String[] sNodes = s.split(";");
        List<Integer> Inodes = new ArrayList<>();
        for (String sNode : sNodes) { Inodes.add(Integer.parseInt(sNode)); }
        return Inodes;
    }

    public Path copy(){
        Path p = new Path();
        p.nodes = this.nodes;
        p.travelTime = this.travelTime;
        return p;
    }
}
