import java.util.*;

public class Main {
    public static void main(String[] args) {
        Map<double[],double[]> dataset = new HashMap<>(){{
           put(new double[]{0.0,0.0}, new double[]{0.0});
           put(new double[]{0.0,1.0}, new double[]{1.5});
           put(new double[]{1.0,0.0}, new double[]{0.5});
           put(new double[]{1.0,1.0}, new double[]{2.0});
        }};

        int epochs = 100;

        int inN = 2;
        int outN = 1;
        int n = 100;

        double genomeWeightMutationProbability = 0.8;
        double weightMutationProbability = 0.9;
        double addConnectionProbability = 0.2;
        double addNodeProbability = 0.2;

        Population population = new Population(inN, outN, n, genomeWeightMutationProbability, weightMutationProbability, addConnectionProbability, addNodeProbability);
        population.initPopulation();
        population.setDataset(dataset);
        for(int i = 0; i < epochs; i++) {
            population.stepGeneration();
        }
    }
}