
# Guide d'Installation Détaillé 📦

## Table des matières
1. [Configuration Matérielle](#configuration-matérielle)
2. [Configuration ESP32](#configuration-esp32)
3. [Configuration Android](#configuration-android)
4. [Premier Test](#premier-test)
5. [Dépannage](#dépannage)

---

## Configuration Matérielle

### Composants Nécessaires

| Composant | Modèle Recommandé | Fonction |
|-----------|-------------------|----------|
| Microcontrôleur | ESP32-S2 DevKit | Cerveau du système |
| Servos | Parallax 360° 32311 | Moteurs de contrôle |
| Batterie | LiPo 7.4V / 2000mAh | Alimentation |
| Châssis | Voiture RC 1:18 | Structure physique |
| Régulateur | LM7805 ou similaire | Stabiliseur 5V |
| Condensateurs | 10µF, 100µF | Filtrage alimentation |

### Schéma de Branchement

```
┌─────────────────────────────────────┐
│         BATTERIE LIPO 7.4V          │
└──────────────┬──────────────────────┘
               │
        ┌──────┴──────┐
        │             │
      ┌─▼─┐         ┌─▼────────┐
      │ + │         │ LM7805   │ (Régulateur)
      └───┘         └──┬───────┘
        │               │
        │              ┌▼─┐
        │ (GND)        │5V│ ──────► Servos
        │              └──┘
        └──────────────┬─────────┐
                       │         │
                     ┌─▼─┐    ┌──▼──┐
                     │GND│    │ESP32│
                     └───┘    └─────┘
                                │
                ┌───────────────┼───────────────┐
                │               │               │
              GPIO25          GPIO26          GPIO34
                │               │               │
            Servo PWM       Servo PWM       Battery ADC
            Throttle         Yaw           (via diviseur)
```

### Diviseur de Tension pour Batterie

Pour lire la tension batterie en ADC (max 3.3V) :

```
Batterie (7.4V) ──[R1=10kΩ]──┬──► GPIO 34
                              │
                            [R2=10kΩ]
                              │
                             GND

Tension ADC = Batterie × R2/(R1+R2) = 7.4V × 10k/20k = 3.7V ✓
```

---

## Configuration ESP32

### Étape 1 : Installation d'Arduino IDE

1. Téléchargez [Arduino IDE](https://www.arduino.cc/en/software)
2. Installez-le sur votre ordinateur

### Étape 2 : Ajouter le Support ESP32

1. Ouvrez Arduino IDE
2. **Fichier → Préférences**
3. Dans **"URLs supplémentaires de gestionnaires de cartes"**, collez :
   ```
   https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json
   ```
4. Cliquez **OK**
5. **Outils → Carte → Gestionnaire de cartes**
6. Recherchez `ESP32` et cliquez **Installer**
7. Attendez la fin de l'installation (~5 minutes)

### Étape 3 : Installer les Dépendances

1. **Sketch → Inclure une bibliothèque → Gérer les bibliothèques**
2. Installez les bibliothèques suivantes :
   - `ESP32Servo` (par Kevin Harrington)
   - `BLEDevice` (pré-installée avec ESP32)

### Étape 4 : Configurer la Carte

1. **Outils → Carte** : Sélectionnez `ESP32S2 Dev Module`
2. **Outils → Port** : Sélectionnez le port COM (ex: COM3)
3. **Outils → Vitesse de téléversement** : `460800` (ou `921600`)
4. **Outils → Flash Mode** : `QIO`

### Étape 5 : Télécharger le Code

1. Clonez ou téléchargez le repository
2. Ouvrez `esp32/ESP32_RC_Controller.ino`
3. Cliquez le bouton **Vérifier** (✓) pour compiler
4. Cliquez le bouton **Téléverser** (→) pour télécharger

```
Attendre le message : "Leaving... Hard resetting via RTS pin..."
```

### Étape 6 : Vérifier la Connexion

1. **Outils → Moniteur Série** (ou `Ctrl+Shift+M`)
2. Réglez la vitesse sur `115200`
3. Vous devriez voir :
   ```
   BLE Server started. Waiting for connections...
   ```

---

## Configuration Android

### Prérequis Système

- **Android 7.0** ou plus récent
- **1 GB de RAM** minimum
- **100 MB d'espace libre**

### Étape 1 : Installation d'Android Studio

1. Téléchargez [Android Studio](https://developer.android.com/studio)
2. Installez-le avec les outils SDK

### Étape 2 : Ouvrir le Projet

1. Lancez Android Studio
2. **File → Open** et sélectionnez le dossier `android/`
3. Attendez que Gradle se synchronise

### Étape 3 : Configurer l'Appareil Cible

#### Option A : Téléphone Physique
1. Activez le **Mode Développeur** :
   - Android 10+ : **Paramètres → À propos → Numéro de build** (appuyez 7x)
   - Android 9- : **Paramètres → À propos du téléphone → Numéro de version** (appuyez 7x)
2. **Paramètres → Options pour développeurs → Débogage USB** : Activé
3. Branchez le téléphone par USB

#### Option B : Émulateur
1. **Tools → Device Manager**
2. Cliquez **Create Device**
3. Sélectionnez un appareil récent (Pixel 6+)
4. Lancez l'émulateur

### Étape 4 : Compiler et Installer

```bash
cd android/
./gradlew assembleDebug
./gradlew installDebug
```

Ou avec Android Studio :
1. Cliquez **Run > Run 'app'** (ou `Shift+F10`)
2. Sélectionnez votre appareil
3. L'app se lance automatiquement

### Étape 5 : Accorder les Permissions

À la première ouverture, l'app vous demandera :
- **Bluetooth Scan**
- **Bluetooth Connect**
- **Localisation (Fine)**

Acceptez toutes les permissions.

---

## Premier Test

### Checklist Pré-Test

- [ ] ESP32 téléchargé et montrant "Waiting for connections..."
- [ ] Application Android installée
- [ ] Bluetooth activé sur le téléphone
- [ ] Batterie chargée (au moins 5V)
- [ ] Servos branchés sur GPIO 25 et 26

### Procédure de Test

1. **Lancez l'application Android**
2. Vous devriez voir l'écran principal avec **"Mode Paysage"**
3. **En haut à droite**, un point rouge devrait clignoter (pas connecté)
4. L'app cherche automatiquement l'ESP32 dans les 5 secondes
5. Le point rouge devrait devenir **vert** (connecté) ✓
6. Testez les joysticks :
   - **Joystick gauche** : Les servos devraient bouger
   - **Joystick droit** : Confirmez le mouvement
7. Vérifiez les indicateurs :
   - Batterie (en % en haut à droite)
   - Signal BLE (barres à côté de la batterie)

### Résultats Attendus

| Action | Résultat |
|--------|----------|
| Allumer l'app | Point rouge clignotant |
| Attendre 5s | Point vert fixe (connecté) |
| Joystick haut | Servo throttle vers 180° |
| Joystick bas | Servo throttle vers 0° |
| Joystick droit | Servo yaw vers 180° |
| Joystick gauche | Servo yaw vers 0° |

---

## Dépannage

### L'ESP32 ne s'affiche pas dans l'IDE

**Symptôme** : Port COM non visible dans **Outils → Port**

**Solutions** :
1. Installez les drivers CH340 :
   - [Télécharger ici](https://sparks.gogo.co.nz/ch340.html)
2. Redémarrez l'ordinateur
3. Essayez un autre câble USB (certains ne transmettent que la puissance)

### Erreur de compilation "espressif"

**Solution** :
```bash
Outils → Effacer le cache de l'IDE
Réinstaller Arduino 1.8.19 (version stable)
```

### Application Android ne trouve pas l'ESP32

**Symptôme** : Point rouge reste rouge après 10s

**Solutions** :
1. Vérifiez : **Paramètres → Bluetooth** → L'ESP32 devrait être visible
2. Oubliez l'appareil, puis reconnectez
3. Redémarrez l'ESP32 (débranchez 5s puis rebranchez)
4. Vérifiez l'alimentation de l'ESP32 (LED rouge doit être allumée)

### Servos ne bougent pas

**Solutions** :
1. Testez indépendamment avec ce code :
   ```cpp
   Servo test;
   test.attach(25);
   test.write(90);
   delay(1000);
   test.write(180);
   ```
2. Vérifiez l'alimentation 5V : mesurer avec un multimètre
3. Essayez sur GPIO 27 et 32 (au lieu de 25 et 26)

### Batterie s'affiche 0%

**Solution** :
Ajustez la formule de conversion ADC dans l'ESP32 :
```cpp
int batteryPercent = map(rawValue, CALIBRATION_MIN, CALIBRATION_MAX, 0, 100);
```

Calibrez avec une batterie connue (ex: 7.4V mesuré au multimètre).

---

**Félicitations ! 🎉 Votre système RC est prêt à l'emploi !**
