import java.util.*;

public class ComputationalGraph {
    private List<Node> nodes;
    public ComputationalGraph(Genome genome) {
        this.nodes = new ArrayList<>();
        this.buildGraph(genome);
    }

    private void buildGraph(Genome genome){
        List<NodeGene> nodeGenes = genome.getNodes();
        for(NodeGene gene : nodeGenes){
            //Gets the children of this current NodeGene
            Map<NodeGene,Double> childMap = genome.getConnections(gene);
            List<NodeGene> childNodes = childMap.keySet().stream().toList();
            List<Node> nodeChildren = new ArrayList<>();
            for(NodeGene child : childNodes) {
                List<Node> existingNodes = this.nodes.stream().filter(node -> node.getNodeN()==child.getNodeN()).toList();
                if (existingNodes.isEmpty()) {
                    //i.e. if this child node has not yet been created.
                    //Create this node, and add it to the nodeChildren list.
                    Node node = new Node(child.isInputNode(), child.isOutputNode(), gene.getNodeN());
                    this.nodes.add(node);
                    nodeChildren.add(node);
                }else{
                    nodeChildren.add(existingNodes.get(0));
                }
            }
            Node node = new Node(gene.isInputNode(), gene.isOutputNode(), gene.getNodeN());
            int i = 0;
            for(Node child : nodeChildren) {
                node.addChild(child, childMap.get(childNodes.get(i)));
                i++;
            }
            this.nodes.add(node);
        }
    }

    private List<Node> getOutputNodes(){
        return this.nodes.stream().filter(Node::isOutputNode).toList();
    }

    public List<Double> feedforward(double[] inputs){
        List<Node> outputNodes = this.getOutputNodes();
        List<Double> outputs = new ArrayList<>();
        for(int i=0;i<outputNodes.size();i++){
            //output = SUM(child.calc(inputs))
            //NODE side: if node.isInput: return weight*input
            outputs.add(outputNodes.get(i).calc(inputs));
        }
        return outputs;
    }

    public void printGraph(){
        List<Node> outputNodes = this.getOutputNodes();
        for(Node node : outputNodes){
            System.out.println("Output Node:");
            System.out.println(node.toString());
        }
    }

    private double MSE(List<Double> networkOutput, double[] targetValues){
        double r = 0.0;
        for(int i=0; i<networkOutput.size(); i++){
            r += Math.pow(networkOutput.get(i)-targetValues[i], 2);
        }
        return r;
    }

    public double loss(Map<double[],double[]> dataset){
        List<double[]> inputs = dataset.keySet().stream().toList();
        List<double[]> outputs = dataset.values().stream().toList();
        double r = 0;
        for(int i=0; i<inputs.size(); i++){
            List<Double> networkOutput = this.feedforward(inputs.get(i));
            r += this.MSE(networkOutput,outputs.get(i));
        }
        return r;
    }
}
