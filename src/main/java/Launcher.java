/**
 * Der {@code Launcher} dient als separater Einstiegspunkt für die Anwendung.
 * <p>
 * Er wird primär verwendet, um Kompatibilitätsprobleme beim Starten von
 * JavaFX-Anwendungen aus bestimmten Entwicklungsumgebungen (wie z. B. als JAR-Datei
 * oder in einigen IDE-Konfigurationen) zu umgehen, wenn die Hauptklasse direkt von
 * {@code Application} erbt.
 * </p>
 *
 * @author Benjamin Fischer (erstellt unter Verwendung von KI-Assistenz)
 * @version 1.0
 */
public class Launcher {

    /**
     * Die Hauptmethode (Main-Methode), die beim Starten des Programms von der
     * JVM aufgerufen wird. Sie reicht die Argumente direkt an die eigentliche
     * JavaFX-Hauptanwendung weiter.
     *
     * @param args die anwendungsspezifischen Befehlszeilenargumente
     */
    public static void main(String[] args) {
        MainApp.main(args);
    }
}