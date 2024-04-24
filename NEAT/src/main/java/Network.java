import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import org.apache.commons.collections15.Transformer;
import edu.uci.ics.jung.visualization.renderers.Renderer;


import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.*;


public class Network {
    private Genome genome;
    private int inN;
    private int outN;
    private Population population;
    private double lastLoss;
    private int speciesN;
    public Network(int inN, int outN, Population population){
        this.inN = inN;
        this.outN = outN;
        this.population = population;
        this.genome = new Genome(population, this.inN, this.outN);
        this.genome.initializeGenome();
        this.lastLoss = 0;
        this.speciesN = 0;
    }

    public void setSpeciesN(int speciesN){
        this.speciesN = speciesN;
    }

    public int getSpeciesN(){
        return this.speciesN;
    }

    public void setLastLoss(double d){
        this.lastLoss = d;
    }

    public double getLastLoss(){
        return this.lastLoss;
    }

    public void printInfo(){
        //this.genome.printGenome();
        return;
    }

    public Genome getGenome(){
        return this.genome;
    }

    public ComputationalGraph buildGraph(){
        ComputationalGraph graph = new ComputationalGraph(this.genome);
        return graph;
    }


    public void mutate(double genomeWeightMutationProbability, double weightMutationProbability, double addConnectionProbability, double addNodeProbability, double learningRate){
        //should return a list of the innovations in the form List<Gene>, so that these genes can be added to track innovations in this generation.
        if(Math.random()<genomeWeightMutationProbability){
            //mutate weights
            this.genome.mutateWeights(weightMutationProbability, learningRate);
        }
        if(Math.random()<genomeWeightMutationProbability){
            //mutate weights
            this.genome.mutateBiases(weightMutationProbability, learningRate);
        }

        if(Math.random()<addConnectionProbability){
            //add connection
            this.genome.addConnectionGene();
        }
        if(Math.random()<addNodeProbability){
            //add node
            this.genome.addNodeGene();
        }
    }

    public Network deepCopy(){
        Network network = new Network(this.inN, this.outN, this.population);
        network.genome = this.genome.deepCopy();
        return network;
    }

    public VisualizationImageServer printGraph(){
        DirectedSparseGraph<Integer,String> g = new DirectedSparseGraph<>();
        List<NodeGene> nodeGenes = this.genome.getNodeGenes();
        List<ConnectionGene> connectionGenes = this.genome.getConnectionGenes();
        for(ConnectionGene cg : connectionGenes){
            System.out.println(cg);
        }
        for(NodeGene node : nodeGenes){
            g.addVertex(node.getNodeN());
        }
        DecimalFormat df = new DecimalFormat("0.000");
        System.out.println(connectionGenes.size());
        int i = 0;
        for (ConnectionGene connection : connectionGenes) {
            g.addEdge(i+" w:" + df.format(connection.getWeight()) + ", b:" + df.format(connection.getBias()), connection.getIn(), connection.getOut());
            i++;
        }
        VisualizationImageServer vs =
                new VisualizationImageServer(
                        new CircleLayout(g), new Dimension(200, 400));

        List<Integer> inputNodeNs = this.genome.getInputNodes().stream().map(NodeGene::getNodeN).toList();
        List<Integer> outputNodeNs = this.genome.getOutputNodes().stream().map(NodeGene::getNodeN).toList();
        Transformer<Integer, Paint> vertexPaint = new Transformer<Integer, Paint>() {
            @Override
            public Paint transform(Integer vertex) {
                // Return different colors based on vertex attributes
                if(inputNodeNs.contains(vertex)){
                    return Color.GREEN;
                }
                if(outputNodeNs.contains(vertex)){
                    return Color.RED;
                }
                return Color.BLUE;
            }
        };

        Transformer<String, String> vertexLabelTransformer = new Transformer<String, String>() {
            @Override
            public String transform(String vertex) {
                // Return edge labels based on edge attributes
                return vertex;
            }
        };
        Transformer<String, String> edgeLabelTransformer = new Transformer<String, String>() {
            @Override
            public String transform(String edge) {
                // Return edge labels based on edge attributes
                return edge;
            }
        };
        vs.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
        vs.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<>());
        vs.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        vs.getRenderContext().setEdgeLabelTransformer(edgeLabelTransformer);
        return vs;
    }

}
