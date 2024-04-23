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
            Map<NodeGene, List<Double>> childMap = genome.getConnections(gene);
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
                List<Double> childValues = childMap.get(childNodes.get(i));
                node.addChild(child, childValues.get(0), childValues.get(1));
                i++;
            }
            this.nodes.add(node);
        }
    }

    public List<Node> getOutputNodes(){
        return this.nodes.stream().filter(Node::isOutputNode).toList();
    }

    private List<Node> getInputNodes(){
        return this.nodes.stream().filter(Node::isInputNode).toList();
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

    private void printFeedforward(double[] inputs){
        List<Node> outputNodes = this.getOutputNodes();
        String s = "";
        for(Node outputNode : outputNodes){
            //output = SUM(child.calc(inputs))
            //NODE side: if node.isInput: return weight*input
            s += outputNode.printCalc(inputs);
        }
        System.out.println(s);
    }
/*
    public void printGraph(){
        List<Node> outputNodes = this.getOutputNodes();
        for(Node node : outputNodes){
            System.out.println("Output Node:");
            System.out.println(node.toString());
        }
    }
*/

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

    private List<Node> getNodeLayer(int d){
        return this.nodes.stream().filter(node ->node.depth==d).toList();
    }

    private boolean hasCycle(){
        List<Node> outputNodes = this.getOutputNodes();
        List<Node> visitedSet = new ArrayList<>();
        Stack<Node> stack = new Stack<>();
        for(Node outputNode : outputNodes){
            stack.push(outputNode);
        }
        while(!stack.isEmpty()){
            Node node = stack.pop();
            if(visitedSet.contains(node)){
                return true;
            }
            visitedSet.add(node);
            for(Node childNode : node.getChildren()){
                stack.push(childNode);
            }
        }
        return false;
    }

    public boolean isCyclical(){
        return !this.hasCycle();
    }

    public void printGraph(double[] inputs){
        //graph will have to be printed in the terminal, Java plotting libraries look heavy.
        //this.nodes contains a list of all the nodes
        //we can access the input nodes via this.getInputNodes()

        //if you can give each node a depth, you can get information of the number of layers, and the width of these layers.
        //you get the depth of a node by finding how many it has between itself and the minimum distance output node.
        List<Node> outputNodes = this.getOutputNodes();
        for(Node outputNode : outputNodes){
            outputNode.setDepth(0);
        }
        System.out.println("1");
        List<List<Node>> nodeMap = new ArrayList<>();
        int d = 0;
        while(true){
            List<Node> layer = this.getNodeLayer(d);
            if(layer.isEmpty()){
                break;
            }
            nodeMap.add(layer);
            d+=1;
        }
        System.out.println("1");
        //node map currently contains the output nodes at 0 and further layers going on from that.
        //node map now contains deepest layer first.
        Collections.reverse(nodeMap);
        int maxWidth = this.getMaxWidth(nodeMap);
        for(List<Node> nodeList : nodeMap) {
            this.printNodeLayer(nodeList,maxWidth);
        }
        this.printFeedforward(inputs);
    }

    private int getMaxWidth(List<List<Node>> nodeMap){
        int max = 0;
        for(List<Node> layer : nodeMap){
            if(layer.size()>max){
                max = layer.size();
            }
        }
        return max;
    }

    private void printNodeLayer(List<Node> layer, int maxWidth){
        List<Integer> nodeN = new ArrayList<>();
        String s = "";
        for(Node node : layer){
            s += " N:" + node.getNodeN();
            //+ "Weight: " + node.getWeight() + " Bias: " + node.getBias()
        }
        System.out.println(s);
    }
}
