package neuron;

public interface Function {

    /**
     * Berechnet den Funktionswert.
     */
    double calculate(double value);

    /**
     * Berechnet die Ableitung der Funktion.
     *
     * Standardmäßig ist keine Ableitung bekannt.
     */
    default double derivative(double value) {
        throw new UnsupportedOperationException(
                "Für diese Funktion ist keine Ableitung definiert."
        );
    }
}
