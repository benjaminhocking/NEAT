import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;


import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationImageServer;

public class Main implements ActionListener {
    /*
    TODO:
     [x] Innovation Number:
        [x] Track and compare innovations from this generation so that new innovation numbers
          are not awarded for the same innovation.
     - Crossover
     - Speciation
     */

    private JLabel label;
    private JPanel graphPanel;
    private JFrame frame;
    private JPanel genomePanel;
    private JPanel infoPanel;

    Map<double[],double[]> dataset = new HashMap<>(){{
        put(new double[]{0.0,0.0}, new double[]{0.0});
        put(new double[]{0.0,1.0}, new double[]{1.0});
        put(new double[]{1.0,0.0}, new double[]{1.0});
        put(new double[]{1.0,1.0}, new double[]{12.0});
    }};

    int epochs = 50;

    int inN = 2;
    int outN = 1;
    int n = 50;

    double genomeWeightMutationProbability = 0.5;
    double weightMutationProbability = 0.5;
    double addConnectionProbability = 0.4;
    double addNodeProbability = 0.4;
    double learningRate = 0.05;

    public Main(){
        //Initialises frame.
        this.frame = new JFrame();

        //Initialises our genome panel.
        this.genomePanel = new JPanel(new GridLayout(3,0,5,5));
        this.genomePanel.setBounds(0,200,600,200);

        //Initialises our info panel.
        this.infoPanel = new JPanel(new FlowLayout());
        this.infoPanel.setBorder(BorderFactory.createEmptyBorder(30,30,10,30));
        this.infoPanel.setBounds(0,0,200,200);

        JButton button = new JButton("Start Evolution");
        button.addActionListener(this);
        button.setHorizontalAlignment(JButton.CENTER);
        label = new JLabel("Generation Number: 0");
        label.setHorizontalAlignment(JLabel.CENTER);
        this.infoPanel.add(button);
        this.infoPanel.add(label);

        //Initialises our graph panel.
        this.graphPanel = new JPanel(new GridLayout(1,0,0,0));
        this.graphPanel.setBounds(600,200,1000,1200);

        //Adds our panels to the frame.
        this.frame.add(graphPanel, BorderLayout.WEST);
        this.frame.add(genomePanel, BorderLayout.EAST);
        this.frame.add(infoPanel, BorderLayout.NORTH);

        //Set options for our frame.
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setTitle("NeuroEvolution of Augmenting Topologies");
        this.frame.setLayout(null);

        //Render our frame.
        this.frame.pack();
        this.frame.setSize(new Dimension(1920, 1080));
        this.frame.setVisible(true);
    }

    public static void main(String[] args) {
        new Main();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //start(inN, outN, n, genomeWeightMutationProbability, weightMutationProbability, addConnectionProbability, addNodeProbability, learningRate);
        EvolutionTask evolutionTask = new EvolutionTask(this.frame, this.graphPanel, this.genomePanel, this.label, this.inN, this.outN, this.n, this.genomeWeightMutationProbability, this.weightMutationProbability, this.addConnectionProbability, this.addNodeProbability, this.learningRate, this.dataset, this.epochs);
        evolutionTask.execute();
        label.setText("Generation Number: 0");
    }

    static class EvolutionTask extends SwingWorker<Void,Void>{

        Map<double[],double[]> dataset;

        private Population population;

        int inN;
        int outN;
        int n;
        int epochs;

        double genomeWeightMutationProbability;
        double weightMutationProbability;
        double addConnectionProbability;
        double addNodeProbability;
        double learningRate;
        JFrame frame;
        JLabel label;
        JPanel graphPanel;
        JPanel genomePanel;

        public EvolutionTask(JFrame frame, JPanel graphPanel, JPanel genomePanel, JLabel label, int inN, int outN, int n, double genomeWeightMutationProbability, double weightMutationProbability, double addConnectionProbability, double addNodeProbability, double learningRate, Map<double[],double[]> dataset, int epochs){
            this.frame = frame;
            this.graphPanel = graphPanel;
            this.genomePanel = genomePanel;
            this.label = label;
            this.inN = inN;
            this.outN = outN;
            this.n = n;
            this.genomeWeightMutationProbability = genomeWeightMutationProbability;
            this.weightMutationProbability = weightMutationProbability;
            this.addConnectionProbability = addConnectionProbability;
            this.addNodeProbability = addNodeProbability;
            this.learningRate = learningRate;
            this.dataset = dataset;
            this.epochs = epochs;
            this.population = new Population(inN, outN, n, genomeWeightMutationProbability, weightMutationProbability, addConnectionProbability, addNodeProbability, learningRate);
        }

        @Override
        protected Void doInBackground() throws Exception {
            /*

            this.population.initPopulation();
            this.population.setDataset(dataset);

            this.population.printOrganism(0);
            for (int i = 0; i < epochs; i++) {
                this.population.stepGeneration();
                updateGenerationCount(i);
            }
            this.printGenome(0);
            this.printGenome(1);
            this.updateGraph(0);
            this.updateGraph(1);
            this.population.mate(0,1);
            this.printGenome(0);
            this.updateGraph(0);
            System.out.println(this.population.getNetwork(0).getGenome().getConnectionGenes());
            System.out.println(this.population.getNewInnovations().size());

             */

            this.population.initPopulation();

            Genome g1 = this.population.getNetwork(0).getGenome();
            Genome g2 = this.population.getNetwork(1).getGenome();


            g1.addNodeGene();
            g2.addNodeGene();

            this.updateGraph(0);
            this.updateGraph(1);

            try {
                this.population.mate(0, 1);
            }catch (Exception e){
                System.out.println(e);
            }
            this.updateGraph(0);

            return null;
        }

        private void updateGraphs(){
            for(int i=0;i<this.n;i++){
                //JPanel organismPanel = new JPanel(new GridLayout());
                Network network = this.population.getOrganism(i);
                VisualizationImageServer vs = network.printGraph();
                this.graphPanel.add(vs);
                this.graphPanel.revalidate();
                this.graphPanel.repaint();
                //this.frame.add(organismPanel);
            }
        }

        private void updateGraph(int n){
            Network network = this.population.getNetwork(n);
            VisualizationImageServer vs = network.printGraph();
            System.out.println("2");
            this.graphPanel.add(vs);
            System.out.println("2");
            this.graphPanel.revalidate();
            this.frame.revalidate();
            System.out.println("2");
        }

        private void printGenome(int n){
            Network network = this.population.getNetwork(n);
            Genome genome = network.getGenome();
            List<ConnectionGene> connectionGenes = genome.getConnectionGenes();

            for(ConnectionGene gene : connectionGenes){
                JLabel label = new JLabel("N:"+gene.getInnovationNumber()+" "+gene.getIn() +" -> "+gene.getOut());
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setVerticalAlignment(JLabel.CENTER);
                label.setBackground(Color.blue);
                label.setOpaque(true);
                this.genomePanel.add(label);
            }
            //this.frame.add(this.);
            this.genomePanel.revalidate();
            //this.frame.revalidate();
        }

        private void updateGenerationCount(int i){
            label.setText("Generation: "+i);
        }
    }
}