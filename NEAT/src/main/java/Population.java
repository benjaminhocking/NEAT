import java.util.*;

public class Population {
    private int inN;
    private int outN;
    private int n;
    private int innovationNumber;
    private List<Network> networks;
    private final double genomeWeightMutationProbability;
    private final double weightMutationProbability;
    private final double addConnectionProbability;
    private final double addNodeProbability;
    private double learningRate;
    private Map<double[],double[]> dataset;
    private double generation;
    private List<Gene> newInnovations;

    public Population(int inN, int outN, int n, double genomeWeightMutationProbability, double weightMutationProbability, double addConnectionProbability, double addNodeProbability, double learningRate){
        this.inN = inN;
        this.outN = outN;
        this.n = n;
        this.innovationNumber = 0;
        this.networks = new ArrayList<>();
        this.genomeWeightMutationProbability = genomeWeightMutationProbability;
        this.weightMutationProbability = weightMutationProbability;
        this.addConnectionProbability = addConnectionProbability;
        this.addNodeProbability = addNodeProbability;
        this.learningRate = learningRate;
        this.generation = 0;
        this.newInnovations = new ArrayList<>();
    }

    private void updateInnovations(List<Gene> genes){
        this.newInnovations.addAll(genes);
    }

    private void resetInnovations(){
        this.newInnovations = new ArrayList<>();
    }


    public List<Gene> getNewInnovations(){
        return this.newInnovations;
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

    public void decrementInnovationNumber(){
        this.innovationNumber--;
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
        System.out.println("Generation: "+this.generation);
        int topN = 5;
        List<Network> topNetworks = this.getTop(topN);
        List<Network> newNetworks = new ArrayList<>();
        System.out.println("Top "+topN+" networks:");
        System.out.println("1");
        for(Network network : topNetworks){
            network.printInfo();
        }
        System.out.println("1");
        //System.out.println(topNetworks);
        Integer birthRate = this.n/topN;

        for(Network network : topNetworks){
            newNetworks.add(network);
            //network.mutate(genomeWeightMutationProbability, weightMutationProbability, addConnectionProbability, addNodeProbability, learningRate);
            for(int i = 0; i < birthRate-1; i++){
                Network newNetwork = network.deepCopy();
                newNetwork.mutate(genomeWeightMutationProbability, weightMutationProbability, addConnectionProbability, addNodeProbability, learningRate);
                newNetworks.add(newNetwork);
            }
            System.out.println("1");
        }
        System.out.println("1");
        this.networks = newNetworks;
        this.generation++;
    }

    /**
     * Mates a with b, changing the genome of aand leaving b as is
     * @param a: the index of genome a
     * @param b: the index of genome b, where b is the fitter of the two
     */
    public void mate(int a, int b){
        Network n1 = this.getNetwork(a);
        Network n2 = this.getNetwork(b);
        n1.getGenome().mateWith(n2.getGenome());
    }


    public void setDataset(Map<double[],double[]> dataset){
        this.dataset = dataset;
    }

    public void printOrganism(int n){
        if(n>this.n){
            System.out.println("Illegal Argument: N > Population size.");
        }
        Network organism = this.getNetwork(n);
        ComputationalGraph graph = organism.buildGraph();
        double[] inputs = this.dataset.keySet().stream().toList().get(1);
        graph.printGraph(inputs);
    }

    public Network getOrganism(int n){
        if(n>this.n){
            System.out.println("Illegal Argument: N > Population size.");
        }
        return this.getNetwork(n);
    }

    public void addInnovation(ConnectionGene connectionGene){
        this.newInnovations.add(connectionGene);
    }
}