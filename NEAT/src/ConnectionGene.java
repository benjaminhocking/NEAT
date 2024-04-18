import java.util.*;

public class ConnectionGene extends Gene{
    private int in;
    private int out;
    private double weight;
    private boolean enabled;
    private int innovationN;
    public ConnectionGene(int in, int out, double weight, boolean enabled, int innovationN){
        this.in = in;
        this.out = out;
        this.weight = weight;
        this.enabled = enabled;
        this.innovationN = innovationN;
    }

    public int getIn(){
        return this.in;
    }

    public int getOut(){
        return this.out;
    }

    public double getWeight(){
        return this.weight;
    }

    public void mutateWeight(){
        this.weight += Math.random()-0.5;
    }

    public Gene deepCopy(){
        return new ConnectionGene(this.in, this.out, this.weight, this.enabled, this.innovationN);
    }
}
