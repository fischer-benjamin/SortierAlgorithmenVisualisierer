# **SortierAlgorithmenVisualisierer** 

Der SortierAlgorithmenVisualisierer ist eine interaktive JavaFX-Anwendung, die verschiedene Sortieralgorithmen in Echtzeit visualisiert. Durch die grafische Darstellung als Balkendiagramm und eine synchrone akustische Rückmeldung über MIDI können die Abläufe der Algorithmen intuitiv verstanden werden.

## **Funktionen**

* Vielfältige Algorithmen: Enthält klassische Algorithmen wie BubbleSort, QuickSort, MergeSort, RadixSort, BogoSort und viele mehr.  
* Echtzeit-Visualisierung: Dynamische Anpassung an die Fenstergröße mittels JavaFX Canvas.  
* Akustisches Feedback: Jeder Tausch- oder Vergleichsvorgang wird mit einem Ton untermalt (MIDI-Unterstützung mit verschiedenen Instrumenten wie Piano, Square Wave und Sawtooth).  
* Interaktive Steuerung: Anpassung der Sortiergeschwindigkeit (Delay), der Array-Größe und des Sound-Profils während des Betriebs.  
* Statistik-Tracking: Live-Anzeige von durchgeführten Vergleichen und Tauschoperationen.

  ## **Voraussetzungen**

* Java JDK 17 oder höher.  
* JavaFX SDK 17.0.2 (über Maven verwaltet).

  ## **Installation & Start**

Das Projekt nutzt Maven zur Abhängigkeitsverwaltung.

1. Klone das Repository oder kopiere die Dateien in deine IDE.  
2. Stelle sicher, dass Maven die Abhängigkeiten (javafx-controls) aus der pom.xml geladen hat.  
3. Starte die Anwendung über die Klasse Launcher.java.

   ## **Benutzung**

1. Menge: Wähle die Anzahl der Elemente (Balken) aus, die sortiert werden sollen (empfohlen: 50–500).  
2. Algorithmus: Wähle einen Sortieralgorithmus aus dem Dropdown-Menü.  
3. Sound: Wähle ein Instrumentenprofil aus oder stelle den Modus auf "Mute".  
4. Tempo: Passe den Slider an, um die Geschwindigkeit der Animation in Millisekunden zu steuern.  
5. Start: Klicke auf "Mischen & Sortieren", um den Prozess zu starten. Die Anwendung mischt das Array zuerst und beginnt anschließend mit der Sortierung.

   ## **Projektstruktur**

* Launcher: Sicherer Einstiegspunkt für die JVM.  
* MainApp: Initialisierung der UI und Verwaltung des Lebenszyklus der Anwendung.  
* SortierManager: Herzstück der Anwendung; implementiert die Sortierlogik, MIDI-Ausgabe und Thread-Verwaltung.  
* GuiController: Verantwortlich für das Zeichnen der Balken auf das Canvas.

  ## **Autor & Lizenz**

* Autor: Benjamin Fischer  
* Erstellt unter Verwendung von KI-Assistenz.

  ### **Tipps für die Nutzung:**

* Wenn du das Projekt in einer IDE wie IntelliJ IDEA öffnest, erkennt diese automatisch die pom.xml und konfiguriert das Projekt als Maven-Projekt.  
* Stelle sicher, dass deine System-Soundeinstellungen MIDI-Ausgabe erlauben, damit du die akustischen Signale hören kannst.

Dieses Projekt steht unter der MIT-Lizenz. Weitere Informationen finden sich in der beiliegenden LICENSE-Datei. 
