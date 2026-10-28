package neuron;

public final class Functions {

    private Functions() {
    }


    public static final Function ID = new Function() {

        @Override
        public double calculate(double value) {
            return value;
        }

        @Override
        public double derivative(double value) {
            return 1.0;
        }
    };


    public static final Function RELU = new Function() {

        @Override
        public double calculate(double value) {
            return Math.max(0.0, value);
        }

        @Override
        public double derivative(double value) {
            return value > 0.0 ? 1.0 : 0.0;
        }
    };


    public static final Function SIGMOID = new Function() {
        @Override
        public double calculate(double value) {
            return 1.0 / (1.0 + Math.exp(-value));
        }

        @Override
        public double derivative(double value) {
            double sigmoid = calculate(value);

            return sigmoid * (1.0 - sigmoid);
        }
    };


    public static final Function TANH = new Function() {
        @Override
        public double calculate(double value) {
            return Math.tanh(value);
        }

        @Override
        public double derivative(double value) {
            double tanh = Math.tanh(value);

            return 1.0 - tanh * tanh;
        }
    };


    public static Function step(double theta) {
        return new Function() {

            @Override
            public double calculate(double value) {
                return value >= theta ? 1.0 : 0.0;
            }

            @Override
            public double derivative(double value) {
                throw new UnsupportedOperationException(
                        "Die Schwellwertfunktion ist für "
                                + "Backpropagation ungeeignet."
                );
            }
        };
    }
}