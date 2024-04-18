import java.util.*;

public class Network {
    private Genome genome;
    private int inN;
    private int outN;
    private Population population;
    public Network(int inN, int outN, Population population){
        this.inN = inN;
        this.outN = outN;
        this.population = population;
        this.genome = new Genome(population, this.inN, this.outN);
        this.genome.initializeGenome();
    }

    public void printInfo(){
        System.out.println(this.genome.printGenome());
    }

    public ComputationalGraph buildGraph(){
        ComputationalGraph graph = new ComputationalGraph(this.genome);
        return graph;
    }

    public void mutate(double genomeWeightMutationProbability, double weightMutationProbability, double addConnectionProbability, double addNodeProbability){
        if(Math.random()<genomeWeightMutationProbability){
            //mutate weights
            this.genome.mutateWeights(weightMutationProbability);
        }
        if(Math.random()<addConnectionProbability){
            //add connection
        }
        if(Math.random()<addNodeProbability){
            //add node
        }
    }

    public Network deepCopy(){
        Network network = new Network(this.inN, this.outN, this.population);
        network.genome = this.genome.deepCopy();
        return network;
    }
}
