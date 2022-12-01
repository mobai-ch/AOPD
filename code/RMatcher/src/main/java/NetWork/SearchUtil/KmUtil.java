package NetWork.SearchUtil;

import NetWork.Element.Edge;

import java.util.HashMap;

public class KmUtil {
    private HashMap<Long, Integer> W;
    private HashMap<Long, Integer> T;
    private HashMap<Integer, Long> WO;
    private HashMap<Integer, Long> TO;
    private final double[][] weight;
    private double[] lx, ly, slack;
    private int[] goal, visX, visY;
    private int n, nx, ny;
    private double theta;

    public KmUtil(HashMap<Long, HashMap<Long, Edge>> ET, HashMap<Long, Integer> W, HashMap<Long, Integer> T){
        this.theta = 0.00001;
        this.W = W;
        this.T = T;
        this.WO = new HashMap<>();
        this.TO = new HashMap<>();
        int len = Math.max(W.size(), T.size()) + 1;
        n = len-1; nx = len-1; ny = len-1;
        weight = new double[len][len];
        lx = new double[len];
        ly = new double[len];
        slack = new double[len];
        goal = new int[len];
        visX = new int[len];
        visY = new int[len];
        for(long w: W.keySet()){
            int num = W.get(w);
            this.WO.put(num, w);
        }
        for(long t: T.keySet()){
            int num = T.get(t);
            this.TO.put(num, t);
        }
        for(int i=0; i<weight.length; i++){
            for(int j=0; j<weight.length; j++){
                weight[i][j] = 0.0;
            }
        }
        for(long v1: ET.keySet()){
            for(long v2: ET.get(v1).keySet()){
                int a = this.T.get(v1);
                int b = this.W.get(v2);
                this.weight[a][b] = ET.get(v1).get(v2).getWeight(0);
            }
        }
    }

    private int find(int x){
        visX[x] = 1;
        for(int j=1; j <= ny; j++){
            if(visY[j] == 0){
                double t = lx[x] + ly[j] - weight[x][j];
                if(Math.abs(t) < theta){
                    visY[j] = 1;
                    if(goal[j] == -1 || this.find(goal[j]) == 1){
                        goal[j] = x;
                        return 1;
                    }
                }
                // min - slack
                else if(slack[j] > t){
                    slack[j] = t;
                }
            }
        }
        return 0;
    }

    public double KM(){
        for(int i=0; i< ly.length; i++){
            ly[i] = 0;
            lx[i] = 0;
            goal[i] = -1;
        }
        for(int i=1; i<=nx; i++){
            for(int j=1; j<=ny; j++){
                if(weight[i][j] > lx[i]){
                    lx[i] = weight[i][j];
                }
            }
        }
        for(int i=1; i<=nx; i++){
            // Init slack for find the max weight match
            for(int j=1; j<=ny; j++){
                slack[j] = Double.MAX_VALUE;
            }
            while(true){
                for(int k=0; k<lx.length; k++){
                    visX[k] = 0;
                    visY[k] = 0;
                }
                if(find(i) == 1){
                    break;
                }
                double d = Double.MAX_VALUE;
                for(int j=1; j<=ny; j++){
                    if(visY[j] == 0 && d > slack[j]){
                        d = slack[j];
                    }
                }
                for(int j=1; j<=ny; j++){
                    if(visY[j] == 0){
                        slack[j] -= d;
                    }
                }
                for(int j=1; j<=n; j++){
                    if(visY[j] == 1){
                        ly[j] += d;
                    }
                    if(visX[j] == 1){
                        lx[j] -= d;
                    }
                }
            }
        }
        double ans = 0;
        for(int j=1; j<=ny; j++){
            if(goal[j] != -1){
                ans += weight[goal[j]][j];
            }
        }
        return ans;
    }

    public HashMap<Long, Long> GetMatchResult(){
        HashMap<Long, Long> ret = new HashMap<>();
        for(int i=1; i<goal.length; i++){
            if(WO.containsKey(i) && TO.containsKey(goal[i]) && goal[i] != -1 && weight[goal[i]][i] != 0){
                long w = WO.get(i);
                long t = TO.get(goal[i]);
                ret.put(t, w);
            }
        }
        return ret;
    }
}
