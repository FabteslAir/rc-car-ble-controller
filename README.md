RC Car BLE Controller 🚗
License: MIT
Android
ESP32

Télécommande Bluetooth Low Energy (BLE) pour voiture télécommandée avec interface Android moderne style DJI. Contrôlez votre voiture RC en temps réel avec un design intuitif et réactif.

✨ Caractéristiques
🎮 Interface Intuitive : Joysticks virtuels style DJI en mode paysage
📱 Technologie BLE : Connexion sans fil stable et fiable
🔋 Monitoring Batterie : Affichage en temps réel de la tension batterie
📶 Indicateur Signal : Visualisation de la puissance du signal BLE
🎯 Contrôle Précis : Servos 360° continu pour mouvements avant/arrière et gauche/droite
⚡ Faible Latence : Réponse immédiate aux commandes
🔧 Open Source : Code complet et personnalisable
📋 Prérequis
Matériel
ESP32 S2 (microcontrôleur)
2 Servos 360° continu (pour traction et direction)
Batterie LiPo (recommandé 7.4V ou 11.1V)
Module de puissance pour servos (L9110S ou similaire)
Châssis de voiture RC ou équivalent
Logiciels
Android Studio (Arctic Fox ou plus récent)
Arduino IDE ou PlatformIO
Git pour le contrôle de version
🚀 Guide de Démarrage Rapide
1. Cloner le repository
git clone https://github.com/VOTRE_USERNAME/rc-car-ble-controller.git
cd rc-car-ble-controller
2. Configuration ESP32
Installation des dépendances
Ouvrez Arduino IDE
Fichier > Préférences
Ajoutez cette URL aux "URLs supplémentaires de gestionnaires de cartes" :
https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json

Outils > Carte > Gestionnaire de cartes
Recherchez et installez ESP32
Téléchargement du code
Ouvrez esp32/ESP32_RC_Controller.ino
Sélectionnez la carte : ESP32S2 Dev Module
Sélectionnez le port COM approprié
Cliquez Téléverser (flèche ▶️)
3. Configuration Android
Compilation et installation
cd android/
./gradlew installDebug
Ou avec Android Studio :

Ouvrez le dossier android/ comme projet
Cliquez Run > Run 'app'
Sélectionnez votre appareil Android
🔌 Schéma de Connexion
ESP32 S2
├─ GPIO 25 ──────► Servo Throttle (PWM)
├─ GPIO 26 ──────► Servo Yaw (PWM)
├─ GPIO 34 ──────► Batterie ADC (via diviseur tension)
├─ 5V ───────────► Alimentation servos (via régulateur)
└─ GND ──────────► Masse commune (IMPORTANT!)

Batterie
├─ Positive (V+) ─► Entrée régulateur
└─ Négative (GND) ► Masse commune
Calibration des Servos 360°
Position	Throttle	Yaw
Arrière/Gauche	0°	0°
Arrêt/Centre	90°	90°
Avant/Droite	180°	180°
📱 Utilisation de l'Application
Interface Principale
┌─────────────────────────────────┐
│ RC Voiture  │ Signal: 90% │ 85% │
├─────────────────────────────────┤
│                                 │
│  Joystick Gauche  Joystick Droit│
│  (Throttle/Yaw)   (Pitch/Roll)  │
│                                 │
│  Avance/Arrière   Avant/Arrière │
│  Rotation         Gauche/Droite │
└─────────────────────────────────┘
Contrôles
Joystick	Axe	Fonction
Gauche	Vertical	Throttle (Avance/Arrière)
Gauche	Horizontal	Yaw (Rotation)
Droit	Vertical	Pitch (Avant/Arrière)
Droit	Horizontal	Roll (Gauche/Droite)
🔋 Interprétation des Indicateurs
Batterie
🟢 Verde (>60%) : État optimal
🟡 Jaune (30-60%) : Batterie moyenne
🔴 Rouge (<30%) : Recharger immédiatement
Signal BLE
████ (75-100%) : Signal excellent
███░ (50-75%) : Signal bon
██░░ (25-50%) : Signal faible
█░░░ (<25%) : Signal très faible (risque de déconnexion)
📡 Protocole BLE
Service de Contrôle
UUID: 4fafc201-1fb5-459e-8fcc-c5c9c331914b

Caractéristique: beb5483b-36e1-4688-b7f5-ea07361b26a8
Type: READ, WRITE
Format: 4 bytes (int8)

[Byte 0] Throttle   : -100 à +100
[Byte 1] Yaw        : -100 à +100
[Byte 2] Pitch      : -100 à +100
[Byte 3] Roll       : -100 à +100
Service Batterie (Standard)
UUID: 180f
Caractéristique: 2a19 (Battery Level)
Type: READ, NOTIFY
Format: 1 byte (0-100)
🗂️ Structure du Projet
rc-car-ble-controller/
├── android/                  # Application Android Kotlin
│   ├── app/src/main/
│   │   ├── java/com/example/rccar/
│   │   │   ├── MainActivity.kt
│   │   │   ├── ui/
│   │   │   ├── bluetooth/
│   │   │   ├── models/
│   │   │   └── viewmodel/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
│
├── esp32/                    # Code ESP32
│   └── ESP32_RC_Controller.ino
│
├── docs/                     # Documentation
│   ├── SETUP.md
│   ├── API.md
│   ├── ARCHITECTURE.md
│   └── TROUBLESHOOTING.md
│
└── hardware/                 # Schémas et plans
    ├── wiring-diagram.png
    └── servo-360-specs.pdf
🛠️ Troubleshooting
L'appareil Android ne trouve pas l'ESP32
Vérifiez que le Bluetooth est activé sur l'appareil
Assurez-vous que l'ESP32 a téléchargé correctement le code
Vérifiez les permissions Bluetooth dans les paramètres Android
Les servos ne bougent pas
Vérifiez les câbles PWM sur GPIO 25 et 26
Testez les servos indépendamment avec une autre source PWM
Vérifiez la tension d'alimentation (minimum 5V pour les servos)
Connexion BLE instable
Réduisez la distance entre l'ESP32 et l'appareil Android
Vérifiez les interférences WiFi/Bluetooth
Vérifiez que l'alimentation de l'ESP32 est stable (minimum 5V)
Pour plus de détails, consultez TROUBLESHOOTING.md.

📚 Documentation Complète
SETUP.md : Guide d'installation détaillé
API.md : Documentation du protocole BLE
ARCHITECTURE.md : Architecture du système
TROUBLESHOOTING.md : Guide de dépannage
🤝 Contribution
Les contributions sont les bienvenues ! Pour contribuer :

Fork le repository
Créez une branche (git checkout -b feature/AmazingFeature)
Commit vos changements (git commit -m 'Add some AmazingFeature')
Push vers la branche (git push origin feature/AmazingFeature)
Ouvrez une Pull Request
📝 Licence
Ce projet est sous licence MIT - voir le fichier LICENSE pour plus de détails.

👨‍💻 Auteur
Votre Nom - GitHub Profile
📞 Support
Pour des questions ou des problèmes, ouvrez une issue GitHub.

🙏 Remerciements
Espressif Systems pour ESP32
Google pour Android et Jetpack Compose
Communauté Arduino et ESP32
Made with ❤️ for RC enthusiasts


