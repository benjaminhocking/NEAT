import java.util.*;

public class Population {
    private int inN;
    private int outN;
    private int n;
    private int innovationNumber;
    private List<Network> networks;
    private double genomeWeightMutationProbability;
    private double weightMutationProbability;
    private double addConnectionProbability;
    private double addNodeProbability;
    private Map<double[],double[]> dataset;
    public Population(int inN, int outN, int n, double genomeWeightMutationProbability, double weightMutationProbability, double addConnectionProbability, double addNodeProbability){
        this.inN = inN;
        this.outN = outN;
        this.n = n;
        this.innovationNumber = 0;
        this.networks = new ArrayList<>();
        this.genomeWeightMutationProbability = genomeWeightMutationProbability;
        this.weightMutationProbability = weightMutationProbability;
        this.addConnectionProbability = addConnectionProbability;
        this.addNodeProbability = addNodeProbability;
    }
    public void initPopulation(){
        for(int i = 0; i < this.n; i++){
            this.initialiseNetwork();
        }
    }

    public int getInnovationNumber(){
        return innovationNumber;
    }

    public void incrementInnovationNumber(){
        this.innovationNumber++;
    }

    private void initialiseNetwork(){
        Network network = new Network(this.inN, this.outN, this);
        this.networks.add(network);
    }

    public void printInfo(){
        int i = 0;
        for(Network network : this.networks){
            System.out.println("Network Number: "+i);
            network.printInfo();
            i++;
            System.out.println("-----");
        }
    }

    public Network getNetwork(int index){
        return this.networks.get(index);
    }

    public List<Network> getTop(int n){
        //Gets the top N most fit networks in this population
        Map<Network,Double> networkFitness = new HashMap<>();
        //Loops through each network in the population
        double fitness;
        for(Network network : this.networks){
            //Builds the computational graph for this network
            ComputationalGraph graph = network.buildGraph();
            fitness = graph.loss(this.dataset);
            networkFitness.put(network,fitness);
        }
        List<Map.Entry<Network,Double>> sortFitness = new LinkedList<>(networkFitness.entrySet());
        sortFitness.sort(Map.Entry.comparingByValue());
        for(Double d : sortFitness.stream().map(Map.Entry::getValue).toList().subList(0,n)){
            System.out.println("Fitness: "+d);
        }
        return sortFitness.stream().map(Map.Entry::getKey).toList().subList(0,n);
    }

    public void stepGeneration(){
        int topN = 5;
        List<Network> topNetworks = this.getTop(topN);
        List<Network> newNetworks = new ArrayList<>();
        Integer birthRate = this.n/topN;
        for(Network network : topNetworks){
            network.mutate(genomeWeightMutationProbability, weightMutationProbability, addConnectionProbability, addNodeProbability);
            newNetworks.add(network);
            for(int i = 0; i < birthRate-1; i++){
                Network newNetwork = network.deepCopy();
                newNetwork.mutate(genomeWeightMutationProbability, weightMutationProbability, addConnectionProbability, addNodeProbability);
                newNetworks.add(newNetwork);
            }
        }
        this.networks = newNetworks;
    }

    public void setDataset(Map<double[],double[]> dataset){
        this.dataset = dataset;
    }
}
