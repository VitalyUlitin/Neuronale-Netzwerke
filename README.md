# Bedienungsanleitung

Das Programm ermöglicht das Erstellen, Trainieren und Ausführen vollständig verbundener künstlicher neuronaler Netze. Die Topologie des Netzes wird im Java-Code festgelegt. Trainingsdaten und Gewichtsmatrizen können aus Dateien eingelesen werden. Nach einem Training können die ermittelten Gewichte gespeichert und bei einem späteren Programmstart erneut verwendet werden.

## 1. Aufbau eines Netzwerks

Ein Netzwerk wird zunächst über ein zweidimensionales `Function[][]`-Array definiert. Jede Zeile des Arrays entspricht einem Layer. Jeder Eintrag innerhalb einer Zeile entspricht einem normalen Neuron und legt dessen Aktivierungsfunktion fest.

Beispiel:

```java
Function[][] topology = {
        {Functions.ID, Functions.ID},
        {Functions.SIGMOID, Functions.SIGMOID},
        {Functions.ID}
};
```

Die Bias-Neuronen müssen nicht selbst angegeben werden. Die `NetworkFactory` fügt automatisch zu jedem Layer ausser dem output layer ein bias neuron hinzu.

Das Netzwerk wird anschließend erzeugt mit:

```java
Network network = NetworkFactory.create(topology);
```

Zur Verfügung stehen folgende Aktivierungsfunktionen:

```java
Functions.ID
Functions.RELU
Functions.SIGMOID
Functions.TANH
Functions.step(theta)
```

Die Schwellwertfunktion `step(theta)` kann für einen Forward Pass verwendet werden. Für backpropagation ist die NICHT geeignet, da sie keine für das Lernverfahren nutzbare Ableitung besitzt. Für trainierbare hidden/output neuronen sollten daher `ID`, `RELU`, `SIGMOID` oder `TANH` verwendet werden.

---

## 2. Format der Trainingsdaten

Trainingsdaten werden zeilenweise in einer Datei gespeichert. Eine Zeile enthält zunächst die Inputwerte und anschließend die erwarteten outputwerte.

Beispielsweise bei zwei inputneuronen und einem outputneuron:

```text
0;0;        0;
0;1;        1;
1;0;        1;
1;1;        0;
```

Die Leerzeichen dienen ausschließlich der Lesbarkeit. Die Trennung zwischen Input und Output erfolgt anhand der bereits bekannten Anzahl des erkannten input/output.
Dezimalzahlen werden mit einem Punkt geschrieben.

Ein  Semikolon am Ende ist erlaubt.

Die Trainingsdatei kann beispielsweise als `training.csv` in ressources liegen (bezogen auf intellij).

Sie wird eingelesen mit:

```java
Reader.DataSet data = Reader.readData("/training.csv", network.getInputSize(), network.getOutputSize());
```

Das zurückgegebene `DataSet` enthält:

```java
data.inputs()
```

für alle Eingabevektoren und

```java
data.outputs()
```

für die zugehörigen expected outputs.

---

## 3. Neues Netzwerk trainieren

Vor dem ersten Training benötigt das Netzwerk Anfangsgewichte. Diese können ggf zufällig initialisiert werden:

```java
network.initRandomWeights();
```

Danach kann das Training gestartet werden:

```java
TrainingResult result =
        network.train(
                data.inputs(),
                data.outputs(),
                100000,
                0.01,
                0.00001
        );
```

Die Parameter von `train()` bedeuten in dieser Reihenfolge:

```text
inputs: Trainingsinputs

expectedOutputs: zugehörige expected outputs

maxEpochs:maximale Anzahl vollständiger Trainingsdurchläufe

learningRate: Lernrate der backpropagation

tolerance: Fehlergrenze, bei deren Unterschreitung das Training beendet wird
```

Im Beispiel darf das Training höchstens `100000` Epochen durchführen. Die Lernrate beträgt `0.01`. Das Training gilt als erfolgreich konvergiert, sobald der Gesamtfehler `<=0.00001` ist.

Während einer Epoche werden alle Trainingsdatensätze nacheinander verarbeitet. Nach jedem Datensatz werden durch backpopagation unmittelbar die Gewichte verändert.

Nach jeder vollständigen Epoche wird der Gesamtfehler über alle Trainingsdatensätze bestimmt.

---

## 4. Ergebnis des Trainings

`train()` gibt ein `TrainingResult` zurück:

```java
public record TrainingResult(int epochs, double error, boolean converged)
```

Die Werte können beispielsweise so ausgegeben werden:

```java
System.out.println("Epochen: " + result.epochs());

System.out.println("Fehler: " + result.error());

System.out.println("Konvergiert: " + result.converged());
```

Eine mögliche Ausgabe ist:

```text
Epochen: 91719
Fehler: 9.999077764699594E-6
Konvergiert: true
```

`epochs` gibt an, wie viele Epochen ausgeführt wurden.

`error` enthält den Gesamtfehler nach der letzten Epoche. Dieser wird als Summe der halben quadrierten Differenzen zwischen expected/actual output berechnet.

`converged` gibt an, warum das Training beendet wurde:

```text
true: die gewünschte Fehlertoleranz wurde erreicht

false: maxEpochs wurde erreicht, bevor die fehlertoleranz erreicht wurde
```

---

## 5. Trainierte Gewichte speichern

Nach dem Training befinden sich die erlernten Gewichtswerte direkt im Netzwerk.

Sie können z.B. gespeichert werden mit:

```java
Persistence.saveWeights(network.getLayers(),"savedWeights.csv");
```

Die Datei wird relativ zum aktuellen Arbeitsverzeichnis des Programms erzeugt.

Besteht bereits eine Datei unter diesem Pfad, wird sie überschrieben.

Gespeichert werden ausschließlich die Gewichtsmatrizen einschließlich der bias eights. Die Topologie und Aktivierungsfunktionen werden nicht gespeichert und müssen beim späteren Laden wieder identisch im Code definiert werden.

---

## 6. Format einer Gewichtsdatei

Eine Gewichtsdatei besitzt beispielsweise folgende Struktur:

```text
#Weights

# ebene 1
1.0;2.0;
1.0;-1.0;
# Bias Ebene 1
0.0;0.0;

# ebene 2
1.0;
1.0;
# Bias Ebene 2
0.0;
```

Jeder Block

```text
# ebene n
```

beschreibt die Gewichtsmatrix zwischen einem Layer und dessen Nachfolger.

Intern gilt:

```text
weights[layer][sourceNeuron][targetNeuron]
```

Die Zeilen einer Gewichtsmatrix entsprechen daher den Quellneuronen und die Spalten den Zielneuronen.

Die letzte Zeile einer Matrix enthält die Gewichte des bias neuronen.

Bei einem Netzwerk

```text
2 -> 2 -> 1
```

besitzt die erste Gewichtsmatrix deshalb drei Zeilen und zwei Spalten:

```text
          Hidden 1    Hidden 2

Input 1       w           w
Input 2       w           w
Bias          w           w
```

Die zweite Matrix besitzt ebenfalls drei Zeilen, aber nur eine spalte:

```text
          Output

Hidden 1    w
Hidden 2    w
Bias        w
```

---

## 7. Gespeicherte Gewichte laden

Soll ein bereits trainiertes Netzwerk verwendet werden, wird zunächst wieder dieselbe Topologie erzeugt:

```java
Function[][] topology = {
        {Functions.ID, Functions.ID},
        {Functions.SIGMOID, Functions.SIGMOID},
        {Functions.ID}
};

Network network = NetworkFactory.create(topology);
```

Anschließend werden die gespeicherten Gewichte eingelesen:

```java
double[][][] weights = Reader.readWeights("savedWeights.csv");
```

und dem Netzwerk zugewiesen:

```java
network.setWeights(weights);
```

In diesem Fall darf anschließend nicht erneut

```java
network.initRandomWeights();
```

aufgerufen werden, da dadurch die geladenen Gewichte überschrieben würden.

---

## 8. Forward Pass mit einem trainierten Netzwerk

Nach dem Laden der Gewichte kann ein beliebiger input gesetzt werden:

```java
network.setInput(new double[]{1.0, 0.0});
```

Danach wird der Forward Pass durchgeführt:

```java
double[] output = network.forwardPass();
```

Die Ausgabe kann beispielsweise dargestellt werden mit:

```java
System.out.println(Arrays.toString(output));
```

Der vollständige Ablauf für ein bereits trainiertes Netzwerk lautet damit:

```java
Function[][] topology = {
        {Functions.ID, Functions.ID},
        {Functions.SIGMOID, Functions.SIGMOID},
        {Functions.ID}
};

Network network = NetworkFactory.create(topology);

network.setWeights(Reader.readWeights("savedWeights.csv"));

network.setInput(new double[]{1.0, 0.0});

double[] output = network.forwardPass();

System.out.println(Arrays.toString(output));
```

---

## 9. Vorhandene Gewichte weitertrainieren

Ein bereits trainiertes Netzwerk kann auch weitertrainiert werden.

Dazu werden zunächst wieder die Topologie und die gespeicherten Gewichte geladen:

```java
Network network = NetworkFactory.create(topology);

network.setWeights(Reader.readWeights("savedWeights.csv"));
```

Danach werden die Trainingsdaten eingelesen:

```java
Reader.DataSet data = Reader.readData("/training.csv", network.getInputSize(), network.getOutputSize());
```

Das Training kann anschließend erneut gestartet werden:

```java
TrainingResult result =
        network.train(
                data.inputs(),
                data.outputs(),
                100000,
                0.01,
                0.000001
        );
```

Nach dem Weitertraining können die neuen Gewichte erneut gespeichert werden:

```java
Persistence.saveWeights(network.getLayers(),"savedWeights.csv");
```

Dadurch wird der vorherige Trainingsstand durch den neuen ersetzt.

---

## 10. Vorgegebene Gewichte ohne Training verwenden

Es ist ebenfalls möglich, vorgegebene weights direkt für einen Forward Pass zu nutzen.

Beispielsweise:

```java
Network network = NetworkFactory.create(topology);

network.setWeights(Reader.readWeights("/weights.csv"));

network.setInput(new double[]{1.0, 2.0});

double[] output = network.forwardPass();
```

Wenn sich `weights.csv` in ressources befindet, wird sie über den Classpath gefunden.

Ein Training oder eine zufällige Initialisierung ist hierfür nicht erforderlich.

---

## 11. Laden von Dateien

Der `Reader` versucht beim Einlesen zunächst, die Datei aus den Resources zu laden. Das ist nur eine comfort funktion.

Beispielsweise:

```java
Reader.readWeights("/weights.csv");
```

oder:

```java
Reader.readData("/training.csv",2,1);
```

Falls dort keine entsprechende Resource gefunden wird, wird derselbe String als normaler Dateipfad interpretiert.

Dadurch können sowohl mitgelieferte Ressourcen als auch zur Laufzeit erzeugte Dateien gelesen werden.

Beispielsweise:

```text
/training.csv
```

für eine Resource und

```text
savedWeights.csv
```

für eine zuvor durch `Persistence` erzeugte Datei.

Es sollte vermieden werden, gleichzeitig eine Resource und eine externe Datei mit demselben Namen zu verwenden, da der `Reader` zuerst nach einer resource sucht.

---

## 12. Vollständiges Beispiel für ein neues Training

Ein vollständiger Trainingsablauf kann in `Main` folgendermaßen aussehen:

```java
public class Main {

    static void main() {

        Function[][] topology = {
                {
                        Functions.ID,
                        Functions.ID
                },
                {
                        Functions.SIGMOID,
                        Functions.SIGMOID
                },
                {
                        Functions.ID
                }
        };


        Network network = NetworkFactory.create(topology);


        Reader.DataSet data = Reader.readData("/training.csv", network.getInputSize(), network.getOutputSize());


        network.initRandomWeights();


        TrainingResult result =
                network.train(
                        data.inputs(),
                        data.outputs(),
                        100000,
                        0.01,
                        0.00001
                );


        System.out.println("Epochen: " + result.epochs());

        System.out.println("Fehler: " + result.error());

        System.out.println("Konvergiert: " + result.converged());


        Persistence.saveWeights(network.getLayers(), "savedWeights.csv");
    }
}
```

Der Ablauf ist damit:

```text
Topologie definieren -> network erzeugen -> trainingsdaten laden -> gewichte random init -> traing -> auswertung -> speichern
```

---

## 13. Wichtige Hinweise

Die Topologie des Netzwerks und die geladenen Gewichtsmatrizen müssen zueinander passen. `Network.setWeights()` prüft sowohl die Anzahl der Matrizen als auch deren zeilen- und Spaltenanzahl.

Für ein neues Training muss entweder `initRandomWeights()` oder `setWeights()` aufgerufen werden, bevor `train()` verwendet wird.

`initRandomWeights()` darf nicht nach dem Laden bereits trainierter Gewichte aufgerufen werden, wenn diese erhalten bleiben sollen.

Die Reihenfolge der Trainingsdaten bleibt während des Trainings unverändert. Nach jedem einzelnen Trainingsdatensatz werden die Gewichte unmittelbar aktualisiert.

Ein Training endet entweder durch Erreichen der angegebenen Fehlertoleranz oder durch Erreichen der maximalen Epochzahl.

Die gespeicherten Gewichte stellen den erlernten Zustand des Netzes dar. Die Topologie und Aktivierungsfunktionen müssen separat im Programm definiert werden.

Die Schwellwertfunktion `Functions.step(theta)` ist für einen normalen Forward Pass geeignet, nicht jedoch für die backpropagation trainierbarer hidden oder output neurons.
