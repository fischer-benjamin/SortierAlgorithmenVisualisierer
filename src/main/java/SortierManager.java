import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.Synthesizer;
import java.util.Random;

/**
 * Der {@code SortierManager} verwaltet die Ausführung verschiedener sequentieller
 * Sortieralgorithmen, steuert die visuelle Darstellung auf einem JavaFX-Canvas
 * und erzeugt synchrone akustische Rückmeldungen über die MIDI-Schnittstelle.
 *
 * @author Benjamin Fischer (erstellt unter Verwendung von KI-Assistenz)
 * @version 1.0
 */
public class SortierManager {

    /** Der Controller zur Steuerung der Benutzeroberfläche. */
    private final GuiController controller;

    /** Das Canvas-Objekt, auf dem die Sortierung visualisiert wird. */
    private final Canvas canvas;

    /** Die Verzögerung in Millisekunden zwischen den einzelnen Sortierschritten. */
    private int delayMs = 20;

    /** Zähler für die Anzahl der durchgeführten Vergleiche. */
    private int vergleiche = 0;

    /** Zähler für die Anzahl der durchgeführten Tausch- oder Schreibaktionen. */
    private int tauschaktionen = 0;

    /** Callback-Routine zur Aktualisierung der Statistik-Anzeige in der GUI. */
    private final Runnable statistikUpdateCallback;

    /** Synthesizer für die Erzeugung der MIDI-Töne. */
    private Synthesizer synth;

    /** Der MIDI-Kanal, über den die Töne abgespielt werden. */
    private MidiChannel channel;

    /** Flag, das angibt, ob die akustische Rückmeldung aktiv ist. */
    private boolean soundAktiv = true;

    /** Die ID des aktuell ausgewählten MIDI-Instruments (Standard: 80 = Square Wave). */
    private int aktuellesInstrument = 80;

    /** Interner Zähler für abgespielte Töne, genutzt für die Frequenz-Filterung. */
    private int soundZaehler = 0;

    /**
     * Erstellt einen neuen {@code SortierManager} und initialisiert das MIDI-Soundsystem.
     *
     * @param controller              der zuständige GUI-Controller
     * @param canvas                  das Canvas für die Live-Anzeige
     * @param statistikUpdateCallback Methode zur Aktualisierung der UI-Statistiken
     */
    public SortierManager(GuiController controller, Canvas canvas, Runnable statistikUpdateCallback) {
        this.controller = controller;
        this.canvas = canvas;
        this.statistikUpdateCallback = statistikUpdateCallback;
        initMidi();
    }

    /**
     * Initialisiert das MIDI-System und stellt den Standard-Kanal sowie das Instrument ein.
     * Schlägt die Initialisierung fehl, wird der Sound-Modus automatisch deaktiviert.
     */
    private void initMidi() {
        try {
            this.synth = MidiSystem.getSynthesizer();
            this.synth.open();
            this.channel = synth.getChannels()[0];
            this.channel.programChange(aktuellesInstrument);
        } catch (Exception e) {
            System.out.println("Sound konnte nicht initialisiert werden.");
            this.soundAktiv = false;
        }
    }

    /**
     * Ändert den Sound-Modus live aus der GUI heraus.
     * Unterstützt werden vordefinierte Klangprofile oder das Stummschalten.
     *
     * @param modus der gewünschte Modus ("Piano", "Square Wave", "Sawtooth" oder "Mute")
     */
    public void setSoundModus(String modus) {
        if (channel == null) return;

        switch (modus) {
            case "Mute":
                this.soundAktiv = false;
                channel.allNotesOff();
                break;
            case "Piano":
                this.soundAktiv = true;
                this.aktuellesInstrument = 0;
                channel.programChange(0);
                break;
            case "Square Wave":
                this.soundAktiv = true;
                this.aktuellesInstrument = 80;
                channel.programChange(80);
                break;
            case "Sawtooth":
                this.soundAktiv = true;
                this.aktuellesInstrument = 81;
                channel.programChange(81);
                break;
        }
    }

    /**
     * Spielt einen dem Array-Wert proportionalen MIDI-Ton ab.
     * Enthält einen dynamischen Filter, um bei sehr großen Datenmengen ein Übersteuern zu verhindern.
     *
     * @param wert    der aktuelle Wert des Array-Elements
     * @param maxWert der maximale Wert im Array zur Skalierung der Tonhöhe
     */
    private void spieleTonFürWert(int wert, int maxWert) {
        if (!soundAktiv || channel == null) return;

        soundZaehler++;
        int filterSchritt = Math.max(1, maxWert / 150);
        if (soundZaehler % filterSchritt != 0) return;

        double prozent = (double) wert / maxWert;
        int midiTon = 35 + (int)(prozent * 60);

        channel.allNotesOff();
        channel.noteOn(midiTon, 90);
    }

    /**
     * Setzt die Verzögerungszeit für die Sortierschritte.
     *
     * @param ms die Verzögerung in Millisekunden
     */
    public void setDelay(int ms) {
        this.delayMs = ms;
    }

    /**
     * Liefert die Anzahl der bisher getätigten Vergleiche.
     *
     * @return Anzahl der Vergleiche
     */
    public int getVergleiche() { return vergleiche; }

    /**
     * Liefert die Anzahl der bisher getätigten Tauschaktionen.
     *
     * @return Anzahl der Tauschaktionen
     */
    public int getTauschaktionen() { return tauschaktionen; }

    /**
     * Setzt die internen Zähler für Vergleiche und Tauschaktionen auf Null zurück
     * und aktualisiert die Anzeige in der UI.
     */
    public void resetStats() {
        this.vergleiche = 0;
        this.tauschaktionen = 0;
        Platform.runLater(statistikUpdateCallback);
    }

    /**
     * Tauscht zwei Elemente im Array, erhöht die Tauschstatistik, spielt den passenden
     * Ton ab und aktualisiert die grafische Oberfläche. Pausiert anschließend den Thread
     * basierend auf dem eingestellten Delay.
     *
     * @param array das zu sortierende Array
     * @param i     Index des ersten Elements
     * @param j     Index des zweiten Elements
     */
    private void swapAndPause(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;

        tauschaktionen++;
        Platform.setImplicitExit(true);
        Platform.runLater(statistikUpdateCallback);

        spieleTonFürWert(array[i], array.length);
        Platform.runLater(() -> controller.updateVisualisierungViaCanvas(canvas, array));

        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Hilfsmethode für das Zeichnen und Generieren von Tönen speziell während der Merge-Phase.
     * Nützlich zur Drosselung und Synchronisation bei massiven Datenoperationen.
     *
     * @param array           das betroffene Array
     * @param geaenderterIndex der Index des modifizierten Elements
     */
    public void triggerMergeVisualisierung(int[] array, int geaenderterIndex) {
        tauschaktionen++;
        Platform.runLater(statistikUpdateCallback);

        spieleTonFürWert(array[geaenderterIndex], array.length);
        Platform.runLater(() -> controller.updateVisualisierungViaCanvas(canvas, array));

        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Führt eine visuelle und akustische "Erfolgs-Welle" (Success Sweep) über das fertig sortierte Array aus.
     * Die Gesamtdauer wird dynamisch auf ca. 1,5 Sekunden angepasst, unabhängig von der Array-Größe.
     *
     * @param array das fertig sortierte Array
     */
    public void starteErfolgsWelle(int[] array) {
        if (array == null) return;
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double canvasBreite = canvas.getWidth();
        double canvasHoehe = canvas.getHeight();
        int size = array.length;
        double balkenBreite = canvasBreite / size;
        int maxWert = size;

        double wartezeitProBalken = 1500.0 / size;
        long millis = (long) wartezeitProBalken;
        int nanos = Math.max(0, (int) ((wartezeitProBalken - millis) * 1_000_000));

        for (int i = 0; i < size; i++) {
            final int aktuellerIndex = i;

            spieleTonFürWert(array[i], maxWert);

            Platform.runLater(() -> {
                double proportionalerFaktor = (double) array[aktuellerIndex] / maxWert;
                double balkenHoehe = proportionalerFaktor * canvasHoehe;
                double x = aktuellerIndex * balkenBreite;
                double y = canvasHoehe - balkenHoehe;

                gc.setFill(Color.web("#2ecc71"));
                double berechneteBreite = Math.max(1, balkenBreite - 0.5);
                gc.fillRect(x, y, berechneteBreite, balkenHoehe);
            });

            try {
                if (millis > 0 || nanos > 0) {
                    Thread.sleep(millis, nanos);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (channel != null) {
            channel.allNotesOff();
        }
    }

    /**
     * Mischt das Array zufällig mithilfe des Fisher-Yates-Verfahrens.
     * Jede Vertauschung wird visualisiert.
     *
     * @param array das zu mischende Array
     */
    public void shuffle(int[] array) {
        if (array == null || array.length <= 1) return;
        int n = array.length;
        for (int i = n - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            swapAndPause(array, i, j);
        }
    }

    /**
     * Sortiert das Array mittels des Bubble-Sort-Algorithmus (stabil, $O(n^2)$).
     *
     * @param array das zu sortierende Array
     */
    public void bubbleSort(int[] array) {
        int n = array.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                vergleiche++;
                Platform.runLater(statistikUpdateCallback);
                if (array[j] > array[j + 1]) {
                    swapAndPause(array, j, j + 1);
                }
            }
        }
    }

    /**
     * Sortiert das Array mittels des Selection-Sort-Algorithmus (instabil, $O(n^2)$).
     *
     * @param array das zu sortierende Array
     */
    public void selectionSort(int[] array) {
        int n = array.length;
        for (int i = 0; i < n - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < n; j++) {
                vergleiche++;
                Platform.runLater(statistikUpdateCallback);
                if (array[j] < array[minIndex]) {
                    minIndex = j;
                }
            }
            if (minIndex != i) {
                swapAndPause(array, i, minIndex);
            }
        }
    }

    /**
     * Sortiert das Array mittels des Insertion-Sort-Algorithmus (stabil, $O(n^2)$).
     *
     * @param array das zu sortierende Array
     */
    public void insertionSort(int[] array) {
        int n = array.length;
        for (int i = 1; i < n; i++) {
            int j = i;
            while (j > 0) {
                vergleiche++;
                Platform.runLater(statistikUpdateCallback);
                if (array[j - 1] > array[j]) {
                    swapAndPause(array, j, j - 1);
                    j--;
                } else {
                    break;
                }
            }
        }
    }
    /**
     * Sortiert das Array mittels Cocktail-Shaker-Sort (bidirektionales Bubble-Sort).
     *
     * @param array das zu sortierende Array
     */
    public void cocktailShakerSort(int[] array) {
        boolean getauscht = true;
        int start = 0;
        int ende = array.length - 1;

        while (getauscht) {
            getauscht = false;

            for (int i = start; i < ende; ++i) {
                vergleiche++;
                Platform.runLater(statistikUpdateCallback);
                if (array[i] > array[i + 1]) {
                    swapAndPause(array, i, i + 1);
                    getauscht = true;
                }
            }

            if (!getauscht) break;

            getauscht = false;
            ende--;

            for (int i = ende - 1; i >= start; i--) {
                vergleiche++;
                Platform.runLater(statistikUpdateCallback);
                if (array[i] > array[i + 1]) {
                    swapAndPause(array, i, i + 1);
                    getauscht = true;
                }
            }

            start++;
        }
    }

    /**
     * Sortiert das Array mittels Shell-Sort (Variationsform des Insertion-Sorts mit Gaps).
     *
     * @param array das zu sortierende Array
     */
    public void shellSort(int[] array) {
        int n = array.length;

        for (int gap = n / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i += 1) {
                int temp = array[i];
                int j = i;

                while (j >= gap) {
                    vergleiche++;
                    Platform.runLater(statistikUpdateCallback);

                    if (array[j - gap] > temp) {
                        array[j] = array[j - gap];
                        tauschaktionen++;
                        spieleTonFürWert(array[j], n);
                        final int[] finalArray = array;
                        Platform.runLater(() -> controller.updateVisualisierungViaCanvas(canvas, finalArray));
                        try { Thread.sleep(delayMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                        j -= gap;
                    } else {
                        break;
                    }
                }
                array[j] = temp;
            }
        }
    }

    /**
     * Sortiert das Array mittels Bogo-Sort (Zufallsbasiertes Sortieren, $O((n+1)!)$ im Worst Case).
     * Reines Spaß- und Anschauungsverfahren.
     *
     * @param array das zu sortierende Array
     */
    public void bogoSort(int[] array) {
        while (!istSortiert(array)) {
            shuffle(array);

            try { Thread.sleep(Math.max(1, delayMs)); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    /**
     * Sortiert das Array mittels Radix-Sort (LSD-Variante, nicht-vergleichend, $O(n \cdot k)$).
     *
     * @param array das zu sortierende Array
     */
    public void radixSort(int[] array) {
        if (array == null || array.length == 0) return;

        int max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) max = array[i];
        }

        for (int exp = 1; max / exp > 0; exp *= 10) {
            radixCountingSort(array, exp);
        }
    }

    /**
     * Hilfsmethode (Counting Sort) für Radix-Sort basierend auf der aktuellen Zehnerpotenz.
     *
     * @param array das Array
     * @param exp   die aktuelle Stelle (1, 10, 100, ...)
     */
    private void radixCountingSort(int[] array, int exp) {
        int n = array.length;
        int[] output = new int[n];
        int[] count = new int[10];

        for (int i = 0; i < n; i++) {
            count[(array[i] / exp) % 10]++;
        }

        for (int i = 1; i < 10; i++) {
            count[i] += count[i - 1];
        }

        for (int i = n - 1; i >= 0; i--) {
            output[count[(array[i] / exp) % 10] - 1] = array[i];
            count[(array[i] / exp) % 10]--;
        }

        for (int i = 0; i < n; i++) {
            array[i] = output[i];
            tauschaktionen++;
            Platform.runLater(statistikUpdateCallback);

            spieleTonFürWert(array[i], array.length);

            final int[] finalArray = array;
            Platform.runLater(() -> controller.updateVisualisierungViaCanvas(canvas, finalArray));

            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Startet den Quick-Sort-Algorithmus (Teile-und-Herrsche, instabil, $O(n \log n)$ im Schnitt).
     *
     * @param array das zu sortierende Array
     */
    public void quickSort(int[] array) {
        quickSortAnteil(array, 0, array.length - 1);
    }

    /**
     * Rekursive Hilfsmethode für Quick-Sort.
     *
     * @param array  das zu sortierende Array
     * @param links  der linke Grenzindex
     * @param rechts der rechte Grenzindex
     */
    private void quickSortAnteil(int[] array, int links, int rechts) {
        if (links < rechts) {
            int pivotIndex = partition(array, links, rechts);
            quickSortAnteil(array, links, pivotIndex - 1);
            quickSortAnteil(array, pivotIndex + 1, rechts);
        }
    }

    /**
     * Partitionierungs-Schritt für Quick-Sort. Wählt das rechte Element als Pivot.
     *
     * @param array  das zu sortierende Array
     * @param links  der linke Rand
     * @param rechts der rechte Rand (Pivot-Position)
     * @return der finale Index des Pivot-Elements
     */
    private int partition(int[] array, int links, int rechts) {
        int pivot = array[rechts];
        int i = links - 1;
        for (int j = links; j < rechts; j++) {
            vergleiche++;
            Platform.runLater(statistikUpdateCallback);
            if (array[j] < pivot) {
                i++;
                swapAndPause(array, i, j);
            }
        }
        swapAndPause(array, i + 1, rechts);
        return i + 1;
    }

    /**
     * Startet den sequentiellen Merge-Sort-Algorithmus (stabil, $O(n \log n)$).
     *
     * @param array das zu sortierende Array
     */
    public void mergeSort(int[] array) {
        mergeSortAnteil(array, 0, array.length - 1);
    }

    /**
     * Rekursive Aufteilung für Merge-Sort.
     *
     * @param array  das Array
     * @param links  linker Teilbereich
     * @param rechts rechter Teilbereich
     */
    public void mergeSortAnteil(int[] array, int links, int rechts) {
        if (links < rechts) {
            int mitte = links + (rechts - links) / 2;
            mergeSortAnteil(array, links, mitte);
            mergeSortAnteil(array, mitte + 1, rechts);
            merge(array, links, mitte, rechts);
        }
    }

    /**
     * Mischeffekt (Merge) für Merge-Sort. Führt zwei vorsortierte Teilstücke zusammen.
     *
     * @param array  das Hauptarray
     * @param links  Start-Index
     * @param mitte  Mittel-Index
     * @param rechts End-Index
     */
    public void merge(int[] array, int links, int mitte, int rechts) {
        int n1 = mitte - links + 1;
        int n2 = rechts - mitte;
        int[] L = new int[n1];
        int[] R = new int[n2];
        for (int i = 0; i < n1; ++i) L[i] = array[links + i];
        for (int j = 0; j < n2; ++j) R[j] = array[mitte + 1 + j];
        int i = 0, j = 0;
        int k = links;
        while (i < n1 && j < n2) {
            vergleiche++;
            Platform.runLater(statistikUpdateCallback);
            if (L[i] <= R[j]) {
                array[k] = L[i];
                i++;
            } else {
                array[k] = R[j];
                j++;
            }
            triggerMergeVisualisierung(array, k);
            k++;
        }
        while (i < n1) {
            array[k] = L[i];
            i++;
            triggerMergeVisualisierung(array, k);
            k++;
        }
        while (j < n2) {
            array[k] = R[j];
            j++;
            triggerMergeVisualisierung(array, k);
            k++;
        }
    }



    /**
     * Prüft, ob ein übergebenes Array vollständig aufsteigend sortiert ist.
     *
     * @param array das zu pflegende Array
     * @return {@code true} wenn sortiert, andernfalls {@code false}
     */
    private boolean istSortiert(int[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            vergleiche++;
            if (array[i] > array[i + 1]) return false;
        }
        return true;
    }

    /**
     * Sortiert das Array mittels Gnome-Sort (Gartenzwerg-Sortierung, strukturell ähnlich zu Insertion-Sort).
     *
     * @param array das zu sortierende Array
     */
    public void gnomeSort(int[] array) {
        int n = array.length;
        int index = 0;

        while (index < n) {
            if (index == 0) {
                index++;
            }

            vergleiche++;
            Platform.runLater(statistikUpdateCallback);

            if (array[index] >= array[index - 1]) {
                index++;
            } else {
                swapAndPause(array, index, index - 1);
                index--;
            }
        }
    }


}