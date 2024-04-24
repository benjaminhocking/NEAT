import java.sql.Connection;
import java.util.*;

public class Genome {
    private List<Gene> genes;
    private int nodeCounter;
    private Population population;
    private int inN;
    private int outN;
    private List<String> mutationLog;

    public Genome(Population population, int inN, int outN){
        this.nodeCounter = 0;
        this.genes = new ArrayList<Gene>();
        this.population = population;
        this.inN = inN;
        this.outN = outN;
        this.mutationLog = new ArrayList<>();
    }

    private void logMutation(String s){
        this.mutationLog.add(s);
    }

    public List<String> getMutationLog(){
        return this.mutationLog;
    }


    public int getInputSize(){
        return this.inN;
    }

    public int getOutputSize(){
        return this.outN;
    }

    public int nodesSize(){
        return this.getNodes().size();
    }

    public void initializeGenome(){
        //Initialises a fully connected network with no hidden layer
        for(int i = 0; i < inN; i++){
            this.newNodeGene(true, false);
        }
        for(int i = 0; i < outN; i++){
            this.newNodeGene(false, true);
        }
        for(NodeGene inputNode : this.getInputNodes()){
            for(NodeGene outputNode : this.getOutputNodes()){
                this.newConnectionGene(inputNode, outputNode, Math.random(), 0);
            }
        }
    }

    private boolean isConnected(int nodeN1, int nodeN2){
        List<ConnectionGene> cgs = this.getConnectionGenes();
        for(ConnectionGene gene : cgs){
            if(gene.getIn()==nodeN1 && gene.getOut()==nodeN2){
                return true;
            }
            if(gene.getIn()==nodeN2 && gene.getOut()==nodeN1){
                return true;
            }
        }
        return false;
    }

    private NodeGene newNodeGene(boolean inputNode, boolean outputNode){
        NodeGene node = new NodeGene(this.nodeCounter, inputNode, outputNode);
        this.nodeCounter++;
        this.genes.add(node);
        return node;
    }

    public ConnectionGene newConnectionGene(NodeGene node1, NodeGene node2, double weight, double bias){
        /*
        node1: in
        node2: out
         */
        ConnectionGene connectionGene = new ConnectionGene(node1.getNodeN(), node2.getNodeN(), weight, bias, true, this.population.getInnovationNumber());
        int innovationNumber = this.calculateInnovationNumber(connectionGene);
        connectionGene.setInnovationNumber(innovationNumber);
        this.genes.add(connectionGene);
        return connectionGene;
    }

    public List<ConnectionGene> getDisjointGenes(List<ConnectionGene> c1){
        List<Integer> innovationNs2 = this.getConnectionGenes().stream().map(ConnectionGene::getInnovationNumber).toList();
        return c1.stream().filter(cg -> !innovationNs2.contains(cg.getInnovationNumber())).toList();
    }

    public String stringGenome(){
        String s = "";
        for(Gene gene : this.genes){
            if(gene instanceof ConnectionGene){
                s += " ConnectionGene: " + ((ConnectionGene)gene).toString();
            }
            if(gene instanceof NodeGene){
                s += " NodeGene: " + ((NodeGene)gene).toString();
            }
        }
        return s;
    }

    public void mateWith(Genome genome){
        List<ConnectionGene> genes1 = this.getConnectionGenes();
        List<ConnectionGene> genes2 = genome.getConnectionGenes();
        logMutation("Mate With: " + genome.stringGenome());

        List<ConnectionGene> newGenes = new ArrayList<>();
        //Chooses randomly between each parent for all matching genes.
        for (ConnectionGene gene1 : genes1) {
            for (ConnectionGene gene2 : genes2) {
                if (gene1.getInnovationNumber() == gene2.getInnovationNumber()) {
                    if (Math.random() < 0.5) {
                        newGenes.add(gene1);
                    } else {
                        newGenes.add(gene2);
                    }
                    break;
                }
            }
        }

        List<ConnectionGene> disjointGenes = this.getDisjointGenes(genes2);
        newGenes.addAll(disjointGenes);
        this.reconstituteGenome(newGenes);
    }

    private void emptyGenes(){
        this.genes = new ArrayList<>();
        this.nodeCounter = 0;
    }

    

    private void reconstituteGenome(List<ConnectionGene> connectionGenes){
        //System.out.println("before reconstitution there are "+this.getOutputNodes().size()+" output nodes");
        List<Integer> allNodes = new ArrayList<>();
        for(ConnectionGene connectionGene : connectionGenes){
            if(!allNodes.contains(connectionGene.getIn())){
                allNodes.add(connectionGene.getIn());
            }
            if(!allNodes.contains(connectionGene.getOut())){
                allNodes.add(connectionGene.getOut());
            }
        }
        Map<Integer,NodeGene> currentNodes = this.getNodeGenesMap();
        this.emptyGenes();
        logMutation("After emptying genes there are "+this.genes.size() + " genes");
        //System.out.println("In the currentNodes var we now have "+currentNodes.size() + " nodes");
        for(Integer nodeN : allNodes){
            boolean inputNode = false;
            boolean outputNode = false;
            if(currentNodes.keySet().stream().toList().contains(nodeN)) {
                inputNode = currentNodes.get(nodeN).isInputNode();
                outputNode = currentNodes.get(nodeN).isOutputNode();
            }
            NodeGene nodeGene = new NodeGene(nodeN, inputNode, outputNode);
            logMutation("Reconstitute NodeGene "+nodeN);
            this.genes.add(nodeGene);
            this.nodeCounter++;
        }
        this.genes.addAll(connectionGenes);
        //System.out.println("after reconstitution there are "+this.getOutputNodes().size()+" output nodes");
    }

    private void removeConnectionGene(ConnectionGene connectionGene){
        /*
        node1: in
        node2: out
         */
        this.genes.remove(connectionGene);
    }

    public Map<NodeGene,List<Double>> getConnections(NodeGene nodeGene){
        Map<NodeGene,List<Double>> connections = new HashMap<>();
        for(ConnectionGene connectionGene : this.getConnectionGenes()){
            if(nodeGene.getNodeN() == connectionGene.getOut()){
                List<Double> t = new ArrayList<>();
                t.add(connectionGene.getWeight());
                t.add(connectionGene.getBias());
                connections.put(this.genes.stream().filter(gene -> gene instanceof NodeGene).filter(gene->((NodeGene)gene).getNodeN()==connectionGene.getIn()).map(gene->((NodeGene)gene)).toList().get(0), t);
            }
        }
        return connections;
    }

    public List<NodeGene> getNodes(){
        return this.genes.stream().filter(gene -> gene instanceof  NodeGene).map(gene->((NodeGene)gene)).toList();
    }

    public List<NodeGene> getInputNodes(){
        return this.genes.stream().filter(gene -> gene instanceof NodeGene).filter(gene->((NodeGene)gene).isInputNode()).map(gene->((NodeGene)gene)).toList();
    }

    public List<NodeGene> getOutputNodes(){
        return this.genes.stream().filter(gene -> gene instanceof NodeGene).filter(gene->((NodeGene)gene).isOutputNode()).map(gene->((NodeGene)gene)).toList();
    }

    private int getConnectionNumber(){
        return this.genes.stream().filter(gene-> gene instanceof ConnectionGene).toList().size();
    }

    public void printGenome(){
        //String s = "Input Nodes: "+ this.getInputNodes().size() +"\n" + "Output Nodes: "+ this.getOutputNodes().size() +"\n"+"Connections: "+this.getConnectionNumber();
        System.out.println("Node Genes:");
        for(Gene gene : this.genes){
            if(gene instanceof NodeGene){
                System.out.print(((NodeGene) gene).getNodeN()+ " ");
            }
        }
        System.out.println("\nConnection Genes:");
        for(Gene gene : this.genes){
            if(gene instanceof ConnectionGene){
                System.out.println("W: "+((ConnectionGene) gene).getWeight() + " B: " + ((ConnectionGene) gene).getBias());
            }
        }
    }

    public List<ConnectionGene> getConnectionGenes(){
        return this.genes.stream().filter(gene -> gene instanceof ConnectionGene).map(gene->((ConnectionGene)gene)).toList();
    }

    public List<NodeGene> getNodeGenes(){
        return this.genes.stream().filter(gene -> gene instanceof NodeGene).map(gene->((NodeGene)gene)).toList();
    }

    public Map<Integer,NodeGene> getNodeGenesMap(){
        List<NodeGene> nodeGenes = this.genes.stream().filter(gene -> gene instanceof NodeGene).map(gene->((NodeGene)gene)).toList();
        Map<Integer,NodeGene> res = new HashMap<>();
        for(NodeGene nodeGene : nodeGenes){
            res.put(nodeGene.getNodeN(), nodeGene);
        }
        return res;
    }

    public List<Double> getConnectionWeights(){
        return this.genes.stream().filter(gene -> gene instanceof ConnectionGene).map(gene->((ConnectionGene)gene).getWeight()).toList();
    }

    public void mutateWeights(double weightMutationProbability, double learningRate){
        for(Gene gene : this.genes){
            if(gene instanceof ConnectionGene){
                if(Math.random()<weightMutationProbability){
                    ((ConnectionGene) gene).mutateWeight(learningRate);
                    logMutation("Mutate Weight " + ((ConnectionGene) gene));
                }
            }
        }
    }

    public void mutateBiases(double weightMutationProbability, double learningRate){
        for(Gene gene : this.genes){
            if(gene instanceof ConnectionGene){
                if(Math.random()<weightMutationProbability){
                    ((ConnectionGene) gene).mutateBias(learningRate);
                    logMutation("Mutate Bias " + ((ConnectionGene) gene));
                }
            }
        }
    }

    private NodeGene getRandomNode(List<NodeGene> nodes){
        int index = (int)Math.floor(Math.random()*nodes.size());
        return nodes.get(index);
    }

    private List<NodeGene> getNonConnected(NodeGene a){
        //returns a list of all NodeGenes in this genome that are not connected to node a.
        List<ConnectionGene> connectionGenes = this.getConnectionGenes();
        List<NodeGene> connectedNodes = this.getConnections(a).keySet().stream().toList();
        List<NodeGene> allNodes = this.getNodeGenes();
        return allNodes.stream().filter(node -> !connectedNodes.contains(node)).toList();
    }

    private List<NodeGene> getNonInputNodes(){
        //returns all nodes in this genome that are not input nodes
        List<NodeGene> allNodes = this.getNodeGenes();
        List<NodeGene> inputNodes = this.getInputNodes();
        return allNodes.stream().filter(node -> !inputNodes.contains(node)).toList();
    }

    private List<NodeGene> getNonOutputNodes(){
        //returns all nodes in this genome that are not input nodes
        List<NodeGene> allNodes = this.getNodeGenes();
        List<NodeGene> outputNodes = this.getOutputNodes();
        return allNodes.stream().filter(node -> !outputNodes.contains(node)).toList();
    }


    private boolean isCyclical(){
        System.out.println("3");
        ComputationalGraph cg = new ComputationalGraph(this);
        if(!cg.valid){
            return true;
        }
        System.out.println("3");
        return cg.isCyclical();
    }





    private NodeGene getNodeGene(int nodeN){
        return (NodeGene)this.genes.stream().filter(gene -> gene instanceof NodeGene).filter(gene -> ((NodeGene) gene).getNodeN()==nodeN).toList().get(0);
    }

    private void deleteConnectionGene(ConnectionGene cg){
        this.genes.remove(cg);
    }

    private int calculateInnovationNumber(ConnectionGene newGene){
        List<Gene> innovations = this.population.getNewInnovations();
        for(Gene gene : innovations){
            //check if connectionGene matches any of these genes
            //conditions for a match:
            //in,out are the same.
            if(gene instanceof ConnectionGene){
                if(((ConnectionGene)gene).getIn()==newGene.getIn() && ((ConnectionGene)gene).getOut()==newGene.getOut()){
                    return ((ConnectionGene)gene).getInnovationNumber();
                }
            }
        }
        this.population.addInnovation(newGene);
        this.population.incrementInnovationNumber();
        return this.population.getInnovationNumber();
    }

    /*
    Below are the two structural mutations that are possible.
    When making structural mutations, we must increment the global innovation number (tracked by the Population object).
     */


    public void addConnectionGene(){
        //get two random nodes that are not yet connected to each other.
        NodeGene inNode = this.getRandomNode(this.getNonOutputNodes());
        NodeGene outNode = this.getRandomNode(this.getNonInputNodes());
        if(inNode==outNode){
            return;
        }
        if(!this.isConnected(inNode.getNodeN(), outNode.getNodeN())){
            int preInnovationN = this.population.getInnovationNumber();
            ConnectionGene connectionGene = this.newConnectionGene(inNode, outNode, Math.random(), 0);
            if(this.isCyclical()){
                this.removeConnectionGene(connectionGene);
                if(preInnovationN!=this.population.getInnovationNumber()){
                    this.population.setInnovationNumber(preInnovationN);
                }
            }
            logMutation("Add Connection Gene. InNode: " +inNode + " OutNode: "+outNode + " Connection Gene: "+connectionGene);
        }
    }

    public void addNodeGene(){
        //get an existing connection, add new node c, delete existing connection from a -> b, and create connection a -> c, c -> b.

        List<ConnectionGene> connectionGenes = this.getConnectionGenes();
        ConnectionGene connectionGene =  connectionGenes.get((int)Math.floor(Math.random()*(connectionGenes.size())));

        int inNodeN = connectionGene.getIn();
        NodeGene nodeIn = this.getNodeGene(inNodeN);
        int outNodeN = connectionGene.getOut();
        NodeGene nodeOut = this.getNodeGene(outNodeN);




        //STRUCTURAL INNOVATION
        NodeGene nodeGene = this.newNodeGene(false, false);
        logMutation("Add Node Gene: " + nodeGene);
        this.deleteConnectionGene(connectionGene);

        //connect a -> c, give this a weight of 1.
        //STRUCTURAL INNOVATION
        ConnectionGene ac = this.newConnectionGene(nodeIn, nodeGene, Math.random(), Math.random());
        logMutation("Connect New Node Gene: " + ac);



        //connect c -> b, give this the same weight as the old
        //STRUCTURAL INNOVATION
        ConnectionGene cb = this.newConnectionGene(nodeGene, nodeOut, Math.random(), Math.random());
        logMutation("Connect New Node Gene: " + cb);
    }



    public List<Gene> deepCopyGenes(){
        List<Gene> newGenes = new ArrayList<>();
        for(Gene gene : this.genes){
            if(gene instanceof NodeGene){
                newGenes.add(((NodeGene) gene).deepCopy());
            }else{
                newGenes.add(((ConnectionGene)gene).deepCopy());
            }
        }
        return newGenes;
    }

    private void setNodeCounter(int n){
        this.nodeCounter = n;
    }

    public Genome deepCopy(){
        Genome genome = new Genome(this.population, this.inN, this.outN);
        genome.setNodeCounter(this.nodeCounter);
        genome.genes = this.deepCopyGenes();
        return genome;
    }

    public int getNConnectionGenes(){
        return this.getConnectionGenes().size();
    }

    private double avgWeightDifference(Genome g1){
        List<ConnectionGene> genes = this.getConnectionGenes();
        List<ConnectionGene> genes1 = g1.getConnectionGenes();
        double s = 0;
        double n = 0;
        for (ConnectionGene gene1 : genes) {
            for (ConnectionGene gene2 : genes1) {
                if (gene1.getInnovationNumber() == gene2.getInnovationNumber()) {
                    s += Math.abs(gene1.getWeight() - gene2.getWeight());
                    n++;
                    break;
                }
            }
        }
        return s/n;
    }

    public double delta(Genome g1){
        //parameters
        double c1 = 1.0;
        double c2 = 0.4;

        //Calculates the delta (for use in speciation) between this genome and g1
        //delta = (c1 * D)/N + c2 * W
        double E = this.getDisjointGenes(g1.getConnectionGenes()).size() + g1.getDisjointGenes(this.getConnectionGenes()).size();
        double N = Math.max(this.getNConnectionGenes(), g1.getNConnectionGenes());
        double W = this.avgWeightDifference(g1);

        return (c1 * E)/N + c2 * W;
    }

}
