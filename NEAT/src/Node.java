import java.util.*;

public class Node {
    private Map<Node,Double> children;
    private boolean inputNode;
    private boolean outputNode;
    private int nodeN;
    public Node(boolean inputNode, boolean outputNode, int nodeN){
        this.inputNode = inputNode;
        this.outputNode = outputNode;
        this.nodeN = nodeN;
        this.children = new HashMap<>();
    }

    public int getNodeN(){
        return this.nodeN;
    }

    public void addChild(Node child, Double weight){
        this.children.put(child,weight);
    }

    public boolean isOutputNode(){
        return this.outputNode;
    }

    public double calc(double[] inputs){
        if(this.inputNode){
            return inputs[this.nodeN];
        }
        double sum = 0;
        List<Node> childNodes = this.children.keySet().stream().toList();
        for(Node child : childNodes){
            sum += this.children.get(child) * child.calc(inputs);
        }
        return sum;
    }

    @Override
    public String toString(){
        return "Node{" + "nodeN=" + nodeN + ", children=" + this.children + '}';
    }
}
