package neuron;

public final class Functions {

    public static final Function ID = value -> value;

    public static final Function RELU = value -> Math.max(0.0, value);

    public static final Function SIGMOID = value -> 1.0 / (1.0 + Math.exp(-value));

    public static final Function TANH = Math::tanh;

    public static Function step(double theta) {
        return value -> value >= theta ? 1.0 : 0.0;
    }
}