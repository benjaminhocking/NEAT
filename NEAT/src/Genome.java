import java.util.*;

public class Genome {
    private List<Gene> genes;
    private int nodeCounter;
    private Population population;
    private int inN;
    private int outN;

    public Genome(Population population, int inN, int outN){
        this.nodeCounter = 0;
        this.genes = new ArrayList<Gene>();
        this.population = population;
        this.inN = inN;
        this.outN = outN;
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
                this.newConnectionGene(inputNode, outputNode);
            }
        }
    }

    private void newNodeGene(boolean inputNode, boolean outputNode){
        NodeGene node = new NodeGene(this.nodeCounter, inputNode, outputNode);
        this.nodeCounter++;
        this.genes.add(node);
    }

    private void newConnectionGene(NodeGene node1, NodeGene node2){
        //node1 is in
        //node2 is out
        ConnectionGene connectionGene =  new ConnectionGene(node1.getNodeN(), node2.getNodeN(), Math.random(), true, this.population.getInnovationNumber());
        this.population.incrementInnovationNumber();
        this.genes.add(connectionGene);
    }

    public Map<NodeGene,Double> getConnections(NodeGene nodeGene){
        Map<NodeGene,Double> connections = new HashMap<>();
        for(ConnectionGene connectionGene : this.getConnectionGenes()){
            if(nodeGene.getNodeN() == connectionGene.getOut()){
                connections.put(this.genes.stream().filter(gene -> gene instanceof NodeGene).filter(gene->((NodeGene)gene).getNodeN()==connectionGene.getIn()).map(gene->((NodeGene)gene)).toList().get(0), connectionGene.getWeight());
            }
        }
        return connections;
    }

    public List<NodeGene> getNodes(){
        return this.genes.stream().filter(gene -> gene instanceof  NodeGene).map(gene->((NodeGene)gene)).toList();
    }

    private List<NodeGene> getInputNodes(){
        return this.genes.stream().filter(gene -> gene instanceof NodeGene).filter(gene->((NodeGene)gene).isInputNode()).map(gene->((NodeGene)gene)).toList();
    }

    private List<NodeGene> getOutputNodes(){
        return this.genes.stream().filter(gene -> gene instanceof NodeGene).filter(gene->((NodeGene)gene).isOutputNode()).map(gene->((NodeGene)gene)).toList();
    }

    private int getConnectionNumber(){
        return this.genes.stream().filter(gene-> gene instanceof ConnectionGene).toList().size();
    }

    public String printGenome(){
        String s = "Input Nodes: "+ this.getInputNodes().size() +"\n" + "Output Nodes: "+ this.getOutputNodes().size() +"\n"+"Connections: "+this.getConnectionNumber();
        return s;
    }

    public List<ConnectionGene> getConnectionGenes(){
        return this.genes.stream().filter(gene -> gene instanceof ConnectionGene).map(gene->((ConnectionGene)gene)).toList();
    }

    public List<Double> getConnectionWeights(){
        return this.genes.stream().filter(gene -> gene instanceof ConnectionGene).map(gene->((ConnectionGene)gene).getWeight()).toList();
    }

    public void mutateWeights(double weightMutationProbability){
        for(Gene gene : this.genes){
            if(gene instanceof ConnectionGene){
                if(Math.random()<weightMutationProbability){
                    ((ConnectionGene) gene).mutateWeight();
                }
            }
        }
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

    public Genome deepCopy(){
        Genome genome = new Genome(this.population, this.inN, this.outN);
        genome.genes = this.deepCopyGenes();
        return genome;
    }
}
