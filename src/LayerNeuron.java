public class LayerNeuron extends Neuron implements Function {
    private Function function;

    /** Erstellt ein neues LayerNeuron und initialisiert es mit einer Aktivierungsfunktion.
     * @param function die Aktivierungsfunktion */
    public LayerNeuron(Function function) {
        this.function = function;
    }

    public LayerNeuron() {
        this.function = value -> value; // Default: Identitätsfunktion
    }

    /** Setzt den Wert des Neurons und wendet dabei die Aktivierungsfunktion an.
     * @param value der Eingangswert vor der Aktivierung */
    public void setValue(double value) {
        this.value = calculate(value);
    }

    /** Ändert die Aktivierungsfunktion des Neurons.
     * @param function die neue Aktivierungsfunktion */
    public void setFunction (Function function) {
        this.function = function;
    }

    /** Gibt die aktuell gesetzte Aktivierungsfunktion zurück.
     * @return die aktive Funktion des Neurons */
    public Function getFunction() {
        return this.function;
    }

    /** Berechnet den Aktivierungswert mit der gespeicherten Funktion.
     * @param value der Eingangswert
     * @return der berechnete Aktivierungswert */
    public double calculate(double value) {
        return this.function.calculate(value);
    }
}
