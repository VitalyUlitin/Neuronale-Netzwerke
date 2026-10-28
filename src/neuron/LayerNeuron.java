package neuron;

public class LayerNeuron extends Neuron implements Function {
    private Function function;
    private double preActivationValue;
    private double delta;

    // Erstellt ein neues LayerNeuron und initialisiert es mit einer Aktivierungsfunktion.
    // @param function die Aktivierungsfunktion
    public LayerNeuron(Function function) {
        this.function = function;
    }

    public LayerNeuron() {
        this.function = Functions.ID; // Default: Identitätsfunktion
    }

    // Setzt den Wert des Neurons und wendet dabei die Aktivierungsfunktion an.
    // @param value der Eingangswert vor der Aktivierung
    public void setValue(double value) {
        this.preActivationValue = value;
        this.value = calculate(value);
    }

    // Gibt den Wert vor Anwendung der Aktivierungsfunktion zurück.
    // @return der Eingabewert der Aktivierungsfunktion
    public double getPreActivationValue() {
        return this.preActivationValue;
    }

    // Berechnet die Ableitung der Aktivierungsfunktion am letzten Eingabewert.
    // @return die Ableitung der Aktivierungsfunktion
    public double getActivationDerivative() {
        return this.function.derivative(this.preActivationValue);
    }

    // Gibt das Delta des Neurons zurück.
    // @return das gespeicherte Delta
    public double getDelta() {
        return this.delta;
    }

    // Setzt das Delta des Neurons.
    // @param delta das zu speichernde Delta
    public void setDelta(double delta) {
        this.delta = delta;
    }

    // Ändert die Aktivierungsfunktion des Neurons.
    // @param function die neue Aktivierungsfunktion
    public void setFunction (Function function) {
        this.function = function;
    }

    // Gibt die aktuell gesetzte Aktivierungsfunktion zurück.
    // @return die aktive Funktion des Neurons
    public Function getFunction() {
        return this.function;
    }

    // Berechnet den Aktivierungswert mit der gespeicherten Funktion.
    // @param value der Eingangswert
    // @return der berechnete Aktivierungswert
    public double calculate(double value) {
        return this.function.calculate(value);
    }
}
