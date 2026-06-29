import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.application.Platform;

/**
 * Die Hauptanwendung {@code MainApp} initialisiert die JavaFX-Benutzeroberfläche
 * für das Dashboard zur Sortier-Visualisierung.
 * <p>Sie verwaltet die UI-Steuerelemente zur Anpassung von Array-Größe,
 * Algorithmen-Auswahl, Soundprofilen sowie der Ausführungsgeschwindigkeit.</p>
 *
 * @author Benjamin Fischer (erstellt unter Verwendung von KI-Assistenz)
 * @version 1.0
 */
public class MainApp extends Application {

    /** Das aktuell zu sortierende und zu visualisierende Daten-Array. */
    private int[] testArray;

    /** Der Manager, welcher die Sortieralgorithmen und Audio-Ausgaben steuert. */
    private SortierManager manager;

    /** Der Controller zur grafischen Aufbereitung und Darstellung des Arrays. */
    private GuiController controller;

    /** Das Zeichenfeld (Canvas) für die Balkengrafik. */
    private Canvas canvas;

    /** Eingabefeld für die Anzahl der zu generierenden Balken. */
    private TextField txtAnzahl;

    /** Auswahlmenü für den gewünschten Sortieralgorithmus. */
    private ComboBox<String> comboAlgo;

    /** Auswahlmenü für das akustische Feedback-Profil (MIDI). */
    private ComboBox<String> comboSound;

    /** Schieberegler zur dynamischen Regulierung der Schritt-Verzögerung. */
    private Slider sliderDelay;

    /** Textanzeige für den aktuellen Millisekunden-Wert des Delays. */
    private Label lblDelayWert;

    /** Textanzeige für die Anzahl der durchgeführten Vergleiche. */
    private Label lblVergleiche;

    /** Textanzeige für die Anzahl der durchgeführten Tausch- und Schreibaktionen. */
    private Label lblTausche;

    /** Button zum Starten des kombinierten Misch- und Sortierprozesses. */
    private Button btnStart;

    /** Flag zur Statusüberwachung, um parallele Sortierstarts zu blockieren. */
    private boolean laeuftGerade = false;

    /**
     * Startet die JavaFX-Anwendung, baut den Szenengraphen auf und bindet die
     * UI-Komponenten an die Logik des {@link SortierManager}s.
     *
     * @param primaryStage das Hauptfenster der Anwendung
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sortier-Visualiserer");

        BorderPane root = new BorderPane();
        canvas = new Canvas();

        // Canvas-Größe dynamisch an das Fenster anpassen (abzüglich Kontrollleiste)
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty().subtract(60));
        root.setCenter(canvas);

        controller = new GuiController();
        manager = new SortierManager(controller, canvas, () -> updateLabels());

        // Listener für die Fenster-Skalierung, um das Canvas sofort neu zu zeichnen
        canvas.widthProperty().addListener(ov -> { if(testArray != null) controller.updateVisualisierungViaCanvas(canvas, testArray); });
        canvas.heightProperty().addListener(ov -> { if(testArray != null) controller.updateVisualisierungViaCanvas(canvas, testArray); });

        // --- KONTROLLLEISTE OBEN ERSTELLEN ---
        HBox kontrollLeiste = new HBox(12);
        kontrollLeiste.setStyle("-fx-background-color: #2c3e50; -fx-padding: 10px;");
        kontrollLeiste.setAlignment(Pos.CENTER);

        // 1. Eingabefeld für die Menge der Balken
        Label lblAnzahl = new Label("Menge:");
        lblAnzahl.setStyle("-fx-text-fill: white;");
        txtAnzahl = new TextField("150");
        txtAnzahl.setPrefWidth(50);

        // 2. Dropdown-Menü für den Algorithmus (Bereinigt um Parallel MergeSort)
        Label lblAlgo = new Label("Algo:");
        lblAlgo.setStyle("-fx-text-fill: white;");
        comboAlgo = new ComboBox<>();
        comboAlgo.getItems().addAll(
                "BubbleSort", "SelectionSort", "InsertionSort",
                "QuickSort", "MergeSort", "GnomeSort", "RadixSort",
                "CocktailSort", "ShellSort", "BogoSort"
        );
        comboAlgo.getSelectionModel().selectFirst();

        // 3. Dropdown-Menü für den Sound-Modus befüllen
        Label lblSound = new Label("Sound:");
        lblSound.setStyle("-fx-text-fill: white;");
        comboSound = new ComboBox<>();
        comboSound.getItems().addAll("Piano", "Square Wave", "Sawtooth", "Mute");
        comboSound.getSelectionModel().selectFirst();

        // Listener für Sound-Modus-Wechsel bei Benutzerinteraktion
        comboSound.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (manager != null) {
                manager.setSoundModus(newValue);
            }
        });

        // 4. Schieberegler (Slider) für das Tempo
        Label lblDelay = new Label("Tempo:");
        lblDelay.setStyle("-fx-text-fill: white;");
        sliderDelay = new Slider(1, 200, 15);
        sliderDelay.setPrefWidth(100);

        lblDelayWert = new Label("15 ms");
        lblDelayWert.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");

        // Listener zur Echtzeit-Anpassung der Verzögerung im SortierManager
        sliderDelay.valueProperty().addListener((observable, oldValue, newValue) -> {
            int aktuellesDelay = newValue.intValue();
            lblDelayWert.setText(aktuellesDelay + " ms");
            if (manager != null) {
                manager.setDelay(aktuellesDelay);
            }
        });

        // 5. Start-Button
        btnStart = new Button("Mischen & Sortieren");
        btnStart.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        btnStart.setOnAction(e -> starteAblauf());

        // 6. Labels für die Zähler
        lblVergleiche = new Label("Vergleiche: 0");
        lblVergleiche.setStyle("-fx-text-fill: #f1c40f; -fx-font-weight: bold;");

        lblTausche = new Label("Tauschaktionen: 0");
        lblTausche.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        // Alle Elemente in die Kontrollleiste einfügen
        kontrollLeiste.getChildren().addAll(
                lblAnzahl, txtAnzahl,
                lblAlgo, comboAlgo,
                lblSound, comboSound,
                lblDelay, sliderDelay, lblDelayWert,
                btnStart,
                lblVergleiche, lblTausche
        );
        root.setTop(kontrollLeiste);

        generiereNeuesArray();

        Scene scene = new Scene(root, 1200, 650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Generiert ein neues, sequentiell aufsteigendes Array basierend auf der
     * eingegebenen Elementanzahl. Schützt vor Fehleingaben durch Wertebegrenzung
     * ($5 \le \text{Größe} \le 20000$).
     */
    private void generiereNeuesArray() {
        try {
            int groesse = Integer.parseInt(txtAnzahl.getText());
            if (groesse < 5) groesse = 5;
            if (groesse > 20000) groesse = 20000;
            txtAnzahl.setText(String.valueOf(groesse));

            testArray = new int[groesse];
            for (int i = 0; i < testArray.length; i++) {
                testArray[i] = i + 1;
            }
            controller.updateVisualisierungViaCanvas(canvas, testArray);
        } catch (NumberFormatException e) {
            txtAnzahl.setText("150");
        }
    }

    /**
     * Aktualisiert die Text-Labels für Vergleiche und Tauschaktionen in der UI.
     * Wird als Callback an den {@link SortierManager} übergeben.
     */
    private void updateLabels() {
        lblVergleiche.setText("Vergleiche: " + manager.getVergleiche());
        lblTausche.setText("Tauschaktionen: " + manager.getTauschaktionen());
    }

    /**
     * Startet den gesamten Animationsablauf (Mischen, Pause, Sortieren, Erfolgs-Welle)
     * in einem separaten Worker-Thread, um ein Einfrieren der JavaFX-GUI zu verhindern.
     * Während des Vorgangs werden die Steuerelemente gesperrt.
     */
    private void starteAblauf() {
        if (laeuftGerade) return;
        laeuftGerade = true;
        btnStart.setDisable(true);
        txtAnzahl.setDisable(true);
        comboAlgo.setDisable(true);

        generiereNeuesArray();
        manager.resetStats();

        String gewaehlterAlgo = comboAlgo.getValue();

        new Thread(() -> {
            try {
                // Vor dem Start den aktuell im Dropdown gewählten Sound-Modus erzwingen
                Platform.runLater(() -> manager.setSoundModus(comboSound.getValue()));

                // 1. Schritt: Mischen (mit beschleunigtem Delay für flüssigen Ablauf)
                manager.setDelay(4);
                manager.shuffle(testArray);

                Thread.sleep(600);
                manager.resetStats();

                // 2. Schritt: Sortieren mit eingestelltem UI-Tempo
                manager.setDelay((int) sliderDelay.getValue());

                switch (gewaehlterAlgo) {
                    case "BubbleSort": manager.bubbleSort(testArray); break;
                    case "SelectionSort": manager.selectionSort(testArray); break;
                    case "InsertionSort": manager.insertionSort(testArray); break;
                    case "QuickSort": manager.quickSort(testArray); break;
                    case "MergeSort": manager.mergeSort(testArray); break;
                    case "GnomeSort": manager.gnomeSort(testArray); break;
                    case "RadixSort": manager.radixSort(testArray); break;
                    case "CocktailSort": manager.cocktailShakerSort(testArray); break;
                    case "ShellSort": manager.shellSort(testArray); break;
                    case "BogoSort": manager.bogoSort(testArray); break;
                }

                // 3. Schritt: Der grüne Erfolgs-Sweep
                manager.starteErfolgsWelle(testArray);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                // UI-Komponenten nach der Verarbeitung im JavaFX-Thread wieder freigeben
                Platform.runLater(() -> {
                    laeuftGerade = false;
                    btnStart.setDisable(false);
                    txtAnzahl.setDisable(false);
                    comboAlgo.setDisable(false);
                });
            }
        }).start();
    }

    /**
     * Haupteinstiegspunkt der Java-Anwendung.
     *
     * @param args Startargumente der Kommandozeile
     */
    public static void main(String[] args) {
        launch(args);
    }
}