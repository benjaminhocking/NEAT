import java.util.*;

public class Node {
    private Map<Node,List<Double>> children;
    private boolean inputNode;
    private boolean outputNode;
    private int nodeN;
    public int depth;
    public Node(boolean inputNode, boolean outputNode, int nodeN){
        this.inputNode = inputNode;
        this.outputNode = outputNode;
        this.nodeN = nodeN;
        this.children = new HashMap<>();
        this.depth = 0;
    }

    public int getNodeN(){
        return this.nodeN;
    }

    public List<Node> getChildren(){
        return this.children.keySet().stream().toList();
    }

    public void addChild(Node child, Double weight, Double bias){
        List<Double> t = new ArrayList<>();
        t.add(weight);
        t.add(bias);
        this.children.put(child,t);
    }

    public boolean isOutputNode(){
        return this.outputNode;
    }

    public boolean isInputNode(){
        return this.inputNode;
    }

    private static double sigmoid(double x)
    {
        return 1 / (1 + Math.exp(-x));
    }

    private static double relu(double x){
        return x>0 ? x : 0;
    }

    public double calc(double[] inputs){
        if(this.inputNode){
            return inputs[this.nodeN];
        }
        double sum = 0;
        List<Node> childNodes = this.children.keySet().stream().toList();
        for(Node child : childNodes){
            List<Double> myChild = this.children.get(child);
            double w = myChild.get(0);
            double b = myChild.get(1);
            sum +=  relu(w*(child.calc(inputs)) + b);
        }
        return sum;
    }

    public String printCalc(double[] inputs){
        if(this.inputNode){
            return String.valueOf(inputs[this.nodeN]);
        }
        String sum = "";
        List<Node> childNodes = this.children.keySet().stream().toList();
        for(Node child : childNodes){
            List<Double> myChild = this.children.get(child);
            String w = String.valueOf(myChild.get(0));
            String b = String.valueOf(myChild.get(1));
            sum +=  " + ("+ w +"*" + "(" + child.printCalc(inputs) + ")" +") + " + b;
        }
        return sum;
    }

    public void setDepth(int depth) {
        if (this.depth != 0 && this.depth < depth) {
            return;
        }
        this.depth = depth;
        for (Node child : this.children.keySet()) {
            child.setDepth(depth + 1);
        }
    }

    @Override
    public String toString(){
        return "Node{" + "nodeN=" + nodeN + ", children=" + this.children + '}';
    }
}
