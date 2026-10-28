package network;

public record TrainingResult(
        int epochs,
        double error,
        boolean converged
) {
}