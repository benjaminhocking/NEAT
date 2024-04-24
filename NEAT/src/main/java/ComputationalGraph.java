import java.util.*;


public class ComputationalGraph {
    private List<Node> nodes;
    public boolean valid;
    public ComputationalGraph(Genome genome) {
        this.nodes = new ArrayList<>();
        this.valid = this.buildGraph(genome);
    }



    public List<Integer> getNodeNs(){
        return this.nodes.stream().map(Node::getNodeN).toList();
    }


    private Node connectNode(NodeGene nodeGene, Genome genome){
        //adding a child:
        // - create node
        // - add node to this.nodes
        Node node = new Node(nodeGene.isInputNode(), nodeGene.isOutputNode(), nodeGene.getNodeN());
        this.nodes.add(node);
        Map<NodeGene, List<Double>> childMap = genome.getConnections(nodeGene);
        List<NodeGene> childNodes = childMap.keySet().stream().toList();
        for(NodeGene childNodeGene: childNodes){
            Node child = connectNode(childNodeGene, genome);
            List<Double> childValues = childMap.get(childNodeGene);
            node.addChild(child, childValues.get(0), childValues.get(1));
        }
        return node;
    }

    private boolean buildGraph(Genome genome){
        List<NodeGene> outputNodes = genome.getOutputNodes();
        List<NodeGene> visitedSet = new ArrayList<>();
        Stack<NodeGene> stack = new Stack<>();
        for(NodeGene outputNode : outputNodes){
            stack.push(outputNode);
        }
        while(!stack.isEmpty()){
            NodeGene nodeGene = stack.pop();
            if(visitedSet.contains(nodeGene)){
                return false;
            }
            visitedSet.add(nodeGene);
            Map<NodeGene,List<Double>> childMap = genome.getConnections(nodeGene);
            List<NodeGene> children = childMap.keySet().stream().toList();
            for(NodeGene childNode : children){
                stack.push(childNode);
            }
        }

        //if this genome represents a cyclical graph, this will never terminate.
        //therefore we must track which nodes we have visited, so that id
        for(NodeGene outputNodeGene: genome.getOutputNodes()){
            this.connectNode(outputNodeGene, genome);
        }
        return true;


        /*
        List<NodeGene> nodeGenes = genome.getNodes();
        System.out.println("Building graph with "+genome.getOutputNodes().size() + " output nodes");
        for(NodeGene gene : nodeGenes){
            if(gene.isOutputNode()){
                System.out.println("Output node "+gene);
            }
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
            Node node;
            if(!this.getNodeNs().contains(gene.getNodeN())) {
                node = new Node(gene.isInputNode(), gene.isOutputNode(), gene.getNodeN());
            }else{
                continue;
            }
            int i = 0;
            for(Node child : nodeChildren) {
                List<Double> childValues = childMap.get(childNodes.get(i));
                node.addChild(child, childValues.get(0), childValues.get(1));
                i++;
            }
            this.nodes.add(node);
        }
        List<Integer> encountered = new ArrayList<>();
        for(Node node : this.nodes){
            if(!encountered.contains(node.getNodeN())){
                encountered.add(node.getNodeN());
            }else{
                System.out.println("Duplicate of node "+node.getNodeN());
                String x = node.isOutputNode() ? "IS OUTPUT" : "IS NOT OUTPUT";
                System.out.println("This duplicate " + x);
            }
        }
        System.out.println("after building graph there are " + this.getOutputNodes().size() + " output nodes");

         */
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
        //this.printFeedforward(inputs);
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
        System.out.println("2");
        List<Node> outputNodes = this.getOutputNodes();
        List<Node> visitedSet = new ArrayList<>();
        Stack<Node> stack = new Stack<>();
        System.out.println("2");
        for(Node outputNode : outputNodes){
            stack.push(outputNode);
        }
        System.out.println("2");
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
        System.out.println("2");
        return false;
    }

    public boolean isCyclical(){
        return !this.hasCycle();
    }

    public void printGraph(double[] inputs){
        //this.nodes contains a list of all the nodes
        //we can access the input nodes via this.getInputNodes()

        //if you can give each node a depth, you can get information of the number of layers, and the width of these layers.
        //you get the depth of a node by finding how many it has between itself and the minimum distance output node.
        List<Node> outputNodes = this.getOutputNodes();
        for(Node outputNode : outputNodes){
            outputNode.setDepth(0);
        }
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

    public List<Node> getNodes(){
        return this.nodes;
    }
}
