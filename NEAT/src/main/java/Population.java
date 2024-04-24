import java.util.*;
import java.util.stream.Collectors;

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
    double deltaThreshold;
    private Map<double[],double[]> dataset;
    private double generation;
    private List<Gene> newInnovations;
    private Network champion;
    private double championLoss;

    public Population(int inN, int outN, int n, double genomeWeightMutationProbability, double weightMutationProbability, double addConnectionProbability, double addNodeProbability, double learningRate, double deltaThreshold){
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
        this.deltaThreshold = deltaThreshold;
        this.championLoss = Double.MAX_VALUE;
    }

    private void setChampion(Network champion, Double loss){
        this.champion = champion.deepCopy();
        this.championLoss = loss;
    }

    public Network getChampion(){
        return this.champion;
    }

    public double getChampionLoss(){
        return this.championLoss;
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

    public void setInnovationNumber(int innovationNumber){ this.innovationNumber = innovationNumber; }

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
        int networkN = 0;
        //Gets the top N most fit networks in this population
        Map<Network, Double> networkFitness = new HashMap<>();
        //Loops through each network in the population
        double fitness;
        for (Network network : this.networks) {
            //Builds the computational graph for this network
            ComputationalGraph graph = network.buildGraph();
            fitness = graph.loss(this.dataset);
            network.setLastLoss(fitness);
            networkFitness.put(network, fitness);
        }
        List<Map.Entry<Network, Double>> sortFitness = new LinkedList<>(networkFitness.entrySet());
        sortFitness.sort(Map.Entry.comparingByValue());
        for (Double d : sortFitness.stream().map(Map.Entry::getValue).toList().subList(0, n)) {
            System.out.println("Fitness: " + d);
        }
        return sortFitness.stream().map(Map.Entry::getKey).toList().subList(0, n);
        /*
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("NetworkN: "+networkN);
            Genome thisGenome = this.networks.get(networkN).getGenome();
            thisGenome.printGenome();
            List<String> mutationLog = thisGenome.getMutationLog();
            for(String mutation : mutationLog){
                System.out.println(mutation);
            }
            ComputationalGraph cg = this.networks.get(networkN).buildGraph();
            List<Node> nodes = cg.getNodes();
            for(NodeGene nodeGene : thisGenome.getNodeGenes()){
                System.out.println("____");
                System.out.println(nodeGene);
                System.out.println(nodeGene.isInputNode());
                System.out.println(nodeGene.isOutputNode());
            }
            for(ConnectionGene connectionGene : thisGenome.getConnectionGenes()){
                System.out.println(connectionGene);
            }
            for(Node node : nodes){
                System.out.println(node);
            }
            return new ArrayList<>();
        }

         */
    }

    private List<List<Network>> generatePairs(List<Network> networks, int nPairs){
        if(networks.size()==1){
            List<List<Network>> t = new ArrayList<>();
            List<Network> x = new ArrayList<>();
            x.add(networks.get(0));
            x.add(networks.get(0));
            t.add(x);
            return t;
        }
        int nNetworks = networks.size();
        List<List<Integer>> pairIndices = new ArrayList<>();
        List<List<Network>> res = new ArrayList<>();
        for(int i=0;i<nPairs;i++){
            int i1 = (int)((Math.random()*nNetworks));
            int i2 = (int)((Math.random()*nNetworks));
            while(i2==i1){
                i2 = (int)((Math.random()*nNetworks));
            }
            Network n1 = networks.get(i1);
            Network n2 = networks.get(i2);
            if(n1.getLastLoss()<n2.getLastLoss()){
                res.add(Arrays.asList(n1,n2));
            }else{
                res.add(Arrays.asList(n2,n1));
            }
        }
        return res;
    }

    private List<List<Network>> sortSpecies(){
        List<List<Network>> species = new ArrayList<>();

        for(Network network : this.networks){
            if(species.isEmpty()){
                List<Network> species1 = new ArrayList<>();
                species1.add(network);
                species.add(species1);
                continue;
            }
            boolean speciesFound = false;
            for(int i =0;i<species.size();i++){
                //Compare to a random member of this species.
                Network comparator = species.get(i).get(((int)Math.random()*species.get(i).size()));
                Genome g1 = comparator.getGenome();

                Genome genome = network.getGenome();

                //Calculate the value of delta
                double delta = genome.delta(g1);
                if(delta<this.deltaThreshold){
                    //If delta is below the threshold, add this network to this species
                    species.get(i).add(network);
                    //move on to the next network in the population.
                    speciesFound = true;
                    break;
                }
            }
            if(!speciesFound){
                List<Network> newSpecies = new ArrayList<>();
                newSpecies.add(network);
                species.add(newSpecies);
            }
        }

        return species;
    }

    public void stepGeneration(){
        System.out.println("Generation: "+this.generation);
        System.out.println("Population size: "+this.networks.size());
        List<Network> newNetworks = new ArrayList<>();

        List<List<Network>> species = this.sortSpecies();
        //System.out.println(species);
        //System.out.println("Number of species: " + species.size());

        for(int i =0;i<species.size();i++){
            for(Network network : species.get(i)){
                network.setSpeciesN(i);
            }
        }

        Map<Network,Double> fitnesses = new HashMap<>();

        int speciesN = 0;
        for(List<Network> singleSpecies : species){
            int N = singleSpecies.size();
            Map<Network,Double> speciesFitness = new HashMap<>();
            for(Network network : singleSpecies) {
                ComputationalGraph graph = network.buildGraph();
                double fitness = graph.loss(this.dataset);
                if(fitness<this.championLoss){
                    this.setChampion(network, fitness);
                }
                double adjustedFitness = fitness*N;
                network.setLastLoss(adjustedFitness);
                speciesFitness.put(network, adjustedFitness);
            }
            List<Map.Entry<Network,Double>> sortedFitness = speciesFitness.entrySet().stream().sorted(Map.Entry.comparingByValue()).toList();
            //System.out.println("Species "+speciesN + " min loss: "+sortedFitness.get(0).getValue());

            //let the least performant half die, and regenerate the species by crossover and mutation
            if(N<=2){
                Network n1 = sortedFitness.get(0).getKey();
                newNetworks.add(n1);
                if(N==1){
                    Network newN1 = n1.deepCopy();
                    newN1.mutate(this.genomeWeightMutationProbability, this.weightMutationProbability, this.addConnectionProbability, this.addNodeProbability, this.learningRate);
                    newNetworks.add(newN1);
                }else {
                    Network n2 = sortedFitness.get(1).getKey();
                    Network newN2 = n2.deepCopy();
                    newN2.mutate(this.genomeWeightMutationProbability, this.weightMutationProbability, this.addConnectionProbability, this.addNodeProbability, this.learningRate);
                    newNetworks.add(newN2);
                }
            }
            else {
                int birthN = N / 2;
                int stayingAlive = N - birthN;
                List<Map.Entry<Network, Double>> speciesStaying = sortedFitness.subList(0, stayingAlive);
                //calculate the upper bound for how many children each pair must create
                int childrenEach = (int) Math.ceil(birthN / 2.0);
                int nPairs = (int) Math.floor(stayingAlive / 2.0);

                System.out.println(stayingAlive + " species are staying. We need to birth " + birthN + " more in this generation. Which means each parent must have " + childrenEach);


                List<Network> parents = speciesStaying.stream().map(Map.Entry::getKey).toList();
                newNetworks.addAll(parents);
                List<List<Network>> pairs = this.generatePairs(parents, nPairs);
                System.out.println(pairs);


                try {
                    int childrenMade = 0;
                    for (List<Network> pair : pairs) {
                        //System.out.println("1");
                        Network moreFit = pair.get(0);
                        Network lessFit = pair.get(1);
                        for (int i = 0; i < childrenEach; i++) {
                            //System.out.println("1");
                            if (childrenMade >= birthN) {
                                break;
                            }
                            Network newLessFit = lessFit.deepCopy();
                            this.mate(newLessFit, moreFit);
                            newLessFit.mutate(this.genomeWeightMutationProbability, this.weightMutationProbability, this.addConnectionProbability, this.addNodeProbability, this.learningRate);
                            newNetworks.add(newLessFit);
                            childrenMade++;
                        }
                    }
                    speciesN++;
                } catch (Exception e) {
                    System.out.println("------------------------");
                    System.out.println("------------------------");
                    System.out.println("------------------------");
                    System.out.println("------------------------");
                    System.out.println("------------------------");
                    System.out.println("------------------------");
                    System.out.println("------------------------");


                    e.printStackTrace();
                }
            }
        }
        int cullN = newNetworks.size()-this.n;
        if(cullN>0){
            for(int i=0;i<cullN;i++){
                int index = (int)Math.floor(Math.random()*newNetworks.size());
                newNetworks.remove(index);
            }
        }


        this.networks = newNetworks;
        this.generation++;



        /*
        First we must decide how we create the next species.
        Q: In nature how does this happen?
        A: Two fit organism reproduce to create an offspring. These offspring also mutate.

        So, we should pick the N fittest organisms, and each of the pairs should have this.n/nPairs children, so that we are left with this.n new candidates.

        Some implementations employ elitism, in which the fittest K organisms are automatically carried on to the next generation.
         */

        /*
        List<Network> top5 = this.getTop(topN);
        int nPairs = 3;


        //Gets 3 pairs from this top5 list, such that for each entry the 0th index network is the more fit one.
        List<List<Network>> pairs = this.generatePairs(top5, nPairs);
        for(List<Network> pair : pairs){
            System.out.println("Mating pair: "+this.networks.indexOf(pair.get(0)) + " : " + this.networks.indexOf(pair.get(1)));
        }


        //Number that need to be crossed over is this.n-topN (currently 45)
        //Therefore the number of offspring that each pair must create is
        int crossoverN = this.n-topN;
        System.out.println(topN + " pass on via elitism; "+ crossoverN + " children must be generated through crossover + mutation.");
        int birthRate = (int)Math.ceil(((double)crossoverN)/((double)nPairs));


        newNetworks.addAll(top5);

        int childrenMade = 0;
        for(List<Network> pair : pairs){
            Network moreFit = pair.get(0);
            Network lessFit = pair.get(1);
            for(int i=0;i<birthRate;i++){
                if(childrenMade>=crossoverN){
                    break;
                }
                Network newLessFit = lessFit.deepCopy();
                this.mate(newLessFit, moreFit);
                newLessFit.mutate(this.genomeWeightMutationProbability, this.weightMutationProbability, this.addConnectionProbability, this.addNodeProbability, this.learningRate);
                newNetworks.add(newLessFit);
                childrenMade++;
            }
            if(childrenMade>=crossoverN){
                break;
            }
        }

        this.networks = newNetworks;
        this.generation++;

         */
    }

    public void mate(Network n1, Network n2){
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