public class NodeGene extends Gene{
    private int nodeN;
    private boolean inputNode;
    private boolean outputNode;
    public NodeGene(int nodeN, boolean inputNode, boolean outputNode){
        this.nodeN = nodeN;
        this.inputNode = inputNode;
        this.outputNode = outputNode;
    }
    public int getNodeN() {
        return nodeN;
    }
    public boolean isInputNode() {
        return this.inputNode;
    }

    public boolean isOutputNode() {
        return this.outputNode;
    }

    public Gene deepCopy(){
        return new NodeGene(this.nodeN, this.inputNode, this.outputNode);
    }

    @Override
    public String toString(){
        return String.valueOf(this.nodeN);
    }
}