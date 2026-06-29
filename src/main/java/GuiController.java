import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Der {@code GuiController} ist für die grafische Darstellung der Sortierabläufe zuständig.
 * <p>
 * Er berechnet die Dimensionen der Sortierbalken dynamisch anhand der aktuellen
 * Canvas-Größe und zeichnet das Array-Abbild in Echtzeit auf die Benutzeroberfläche.
 * </p>
 *
 * @author Benjamin Fischer (erstellt unter Verwendung von KI-Assistenz)
 * @version 1.0
 */
public class GuiController {

    /**
     * Aktualisiert die visuelle Darstellung des Arrays auf dem übergebenen Canvas.
     * <p>
     * Die Methode löscht zuerst das gesamte Zeichenfeld und berechnet anschließend
     * für jedes Element des Arrays einen proportional skalierten vertikalen Balken.
     * Die Balkenbreite und -höhe passen sich dabei automatisch an die aktuellen
     * Dimensionen des Canvas an.
     * </p>
     *
     * @param canvas das {@link Canvas}-Objekt, auf dem gezeichnet werden soll
     * @param array  das darzustellende Ganzzahl-Array
     */
    public void updateVisualisierungViaCanvas(Canvas canvas, int[] array) {
        if (canvas == null) return;
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Aktualisiert die aktuelle Canvas Größe
        double canvasBreite = canvas.getWidth();
        double canvasHoehe = canvas.getHeight();

        // Alten Inhalt komplett löschen
        gc.clearRect(0, 0, canvasBreite, canvasHoehe);

        int size = array.length;

        // Dynamische Breite: Gesamte Breite geteilt durch die Anzahl der Balken
        double balkenBreite = canvasBreite / size;

        // Maximalwert suchen für die prozentuale Höhen-Skalierung
        int maxWert = 0;
        for (int wert : array) {
            if (wert > maxWert) maxWert = wert;
        }

        // Balken zeichnen
        for (int i = 0; i < size; i++) {
            // Wie viel Prozent der maximalen Höhe nimmt dieser Balken ein?
            double proportionalerFaktor = (double) array[i] / maxWert;

            // Die Höhe skaliert jetzt perfekt mit der Canvas-Höhe
            double balkenHoehe = proportionalerFaktor * canvasHoehe;

            // X- und Y-Koordinate berechnen
            double x = i * balkenBreite;
            double y = canvasHoehe - balkenHoehe;

            // Setzt die Standardfarbe für die Balken (Klassisches Blau)
            gc.setFill(Color.web("#3498db"));

            // balkenBreite - 0.5 sorgt für eine minimale Lücke, damit man einzelne Balken erkennt
            double berechneteBreite = Math.max(1, balkenBreite - 0.5);

            gc.fillRect(x, y, berechneteBreite, balkenHoehe);
        }
    }
}