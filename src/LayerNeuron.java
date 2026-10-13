public class LayerNeuron extends Neuron implements Function {
    private Function function;

    public LayerNeuron(double value, Function function) {
        this.value = value;
        this.function = function;
    }

    public void setValue(double value) {
        this.value = calculate(this.value);
    }

    public void setFunction (Function function) {
        this.function = function;
    }

    public Function getFunction() {
        return this.function;
    }

    public double calculate(double value) {
        return this.function.calculate(value);
    }
}
