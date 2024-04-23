import java.util.*;

public class ConnectionGene extends Gene{
    private int in;
    private int out;
    private double weight;
    private double bias;
    private boolean enabled;
    private int innovationN;
    public ConnectionGene(int in, int out, double weight, double bias, boolean enabled, int innovationN){
        this.in = in;
        this.out = out;
        this.weight = weight;
        this.enabled = enabled;
        this.innovationN = innovationN;
        this.bias = bias;
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

    public double getBias(){
        return this.bias;
    }

    public void mutateWeight(double learningRate){
        this.weight += (Math.random()-0.5)*learningRate;
    }

    public void mutateBias(double learningRate){
        this.bias += (Math.random()-0.5)*learningRate;
    }

    public Gene deepCopy(){
        return new ConnectionGene(this.in, this.out, this.weight, this.bias, this.enabled, this.innovationN);
    }

    public void setInnovationNumber(int n){
        this.innovationN = n;
    }

    public int getInnovationNumber(){
        return this.innovationN;
    }

    @Override
    public String toString(){
        return this.innovationN + ": " + this.in + " -> " + this.out;
    }
}
