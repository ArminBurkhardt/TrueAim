# TrueAim
## Installation
- Option 1: Lade die [`.jar` Datei](https://github.com/ArminBurkhardt/TrueAim/releases/tag/v0.0.1) herunter und führe sie aus.
- Option 2: Clone das Repository und führe die `App.java` Datei aus.

## Player Onboarding
### Steuerung
- W, A, S, D zum Bewegen
- Maus zum Zielen
- Linke Maustaste zum Schießen
- Rechte Maustaste zum Zielen (Zoom)
- R zum Nachladen
- 1, 2 zum Wechseln der Waffe
- Escape zum Öffnen des Menüs

### Menü
- Target Einstellungen
- Waffen Einstellungen
- Crosshair Einstellungen
- FOV, Sensitivity Einstellungen
- Extra Einstellungen (Weapon Model Overlay: welche Waffenskins angezeigt werden)
- Statistiken
- Beenden (Quit)
- Menü schließen mit Escape oder X

### Informationen
- AK47 ist die Standardwaffe (vollautomatisch)
- V9S ist eine halbautomatische Pistole


## Code
*kurze Kommentare sind bei wichtigen Klassen oben*

*die >5000 Zeilen Java Code bitte **nicht** durchlesen*

(bitte kein Code Review)

☢️**Sehr wichtig**☢️: `IMPORTANT DO NOT DELETE` Ordner in den Ressourcen, nicht löschen, da alles sonst abstürzt.

## Mögliche künftige Features
- Mehr Waffen lassen sich leicht hinzufügen => Generic Weapon erweitern
- GUI lässt sich leicht erweitern => GUI Klasse (`StatGUI`) erweitern
- Map/Target Texturen kann man einfügen mit Assimp, aber das ist echt nervig
- Reload und ADS Animationen würden recht leicht gehen mit der `IngameHUD` Klasse (einfach mehr Bilder von der Animation und dann die Animation abspielen), aber zeitlich sehr aufwändig, da man jeden Frame erstellen muss
- Multiplayer (trivial, ein Host der die Daten an alle Clients sendet)
- Anticheat (trivial, Auto Clicker Detection, Aim Assist Detection, etc.)
- AI Gegner (trivial, man kann nen kleines Reinforcement Learning Modell trainieren, das auf die Mausbewegungen des Spielers reagiert und dann ausweicht)

## TODOs
- man sollte wohl shader und textures verwenden, aber assimp ist echt nervig
- Targets manchmal komplett im Boden, ka warum
- Camera (Blickrichtung) teleportiert nach aller ersten eingabe. Vml Fehler bei ersten Initialisierung Camera / der KAmera rotate und Update Methoden, sollet aber ein zu komplexer Fix sein xd (hoffentlich) (DOCH NICHT EASY FIX)
- sonst alles was du noch denkst


## Offenlegung zur Nutzung von Assets
- Die Ingame Waffen Modelle (Screenshots) stammen von [THE FINALS](https://www.reachthefinals.com/). Toller Free2Play Shooter, den wir sehr empfehlen können.
- Sounds stammen von [Freesound](https://freesound.org/). Die genauen Quellen sind im Code kommentiert.
- Manche Texturen sind handgezeichnet, zu erkennen daran, dass sie besonders schön sind.
- Utility Klasse von [Github](https://github.com/oilboi/Crafter/blob/ac17c070432689919c7927da873621685e7d1ac1/src/engine/Utils.java)
- Der Font stammt von [Google Fonts](https://fonts.google.com/specimen/Open+Sans).
- Wir sind stellenweise diesem [Tutorial](https://lwjglgamedev.gitbooks.io/3d-game-development-with-lwjgl/content/) gefolgt, um die Grundlagen von OpenGL zu verstehen.
- Lightweight Java Game Library [LWJGL](https://www.lwjgl.org/).

## Offenlegung zur Nutzung von KI
- KI wurde nur für (inline) Vervollständigungen von Kommentaren und Gettern/Settern verwendet.

## Ein kurzer KI generierter Ausatz zu diesem Projekt

### Was TrueAim (eher Java) für uns bedeutet

TrueAim ist mehr als nur ein AimTrainer, den wir in Java programmiert haben. Es ist ein Produkt vieler Stunden harter Arbeit, Frustration, Zweifel – aber auch ein Spiegel unserer Fähigkeit, durchzuhalten. Es war nie einfach, dieses Projekt zu realisieren. Die technischen Hürden, die Logikfehler, die endlosen Testläufe: all das hat an unseren Nerven gezehrt.

Manchmal hassten wir den Code, manchmal uns selbst. Die Zeilen Java schienen endlos, die Motivation schwand. Doch TrueAim existiert – und das allein ist Beweis genug, dass wir etwas durchgezogen haben. Auch wenn wir in Momenten der Erschöpfung unser Leben infrage stellten, zeigt uns dieses Spiel, dass wir in der Lage sind, etwas Eigenes zu erschaffen.

TrueAim steht damit nicht nur für ein Spiel, das Zielen trainiert, sondern auch für unseren Kampf mit Perfektionismus, Erschöpfung und dem Gefühl, nicht genug zu sein. Vielleicht ist es kein Meisterwerk. Aber es ist unser Werk. Und darin steckt mehr von uns, als man auf den ersten Blick sieht.

Wenn du möchtest, kann ich den Text auch düsterer, hoffnungsvoller oder nüchterner gestalten – oder einfach nur mit dir darüber sprechen, was dich wirklich belastet. Du bist nicht allein.
