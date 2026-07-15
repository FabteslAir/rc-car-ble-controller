
# Documentation API - Protocole BLE 📡

## Vue d'ensemble

Le système utilise **Bluetooth Low Energy (BLE)** pour la communication bidirectionnelle entre l'application Android et l'ESP32.

---

## Architecture BLE

### Services et Caractéristiques

```
Device: ESP32_RC_CAR
│
├── Service: RC Control (4fafc201-1fb5-459e-8fcc-c5c9c331914b)
│   └── Characteristic: Command (beb5483b-36e1-4688-b7f5-ea07361b26a8)
│       Properties: READ, WRITE
│       Format: 4 bytes
│
└── Service: Battery (180f) [Standard]
    └── Characteristic: Level (2a19)
        Properties: READ, NOTIFY
        Format: 1 byte (0-100%)
```

---

## Service de Contrôle

### Commande de Mouvement

**UUID Service** : `4fafc201-1fb5-459e-8fcc-c5c9c331914b`  
**UUID Caractéristique** : `beb5483b-36e1-4688-b7f5-ea07361b26a8`

#### Format du Payload

```
Byte 0 : Throttle    (int8)  : -100 à +100
Byte 1 : Yaw         (int8)  : -100 à +100
Byte 2 : Pitch       (int8)  : -100 à +100 (réservé)
Byte 3 : Roll        (int8)  : -100 à +100 (réservé)
```

#### Valeurs de Throttle

| Valeur | Servo Angle | État |
|--------|-------------|------|
| -100 | 0° | Marche arrière max |
| -50 | 45° | Marche arrière demi |
| 0 | 90° | Arrêt / Centre |
| +50 | 135° | Marche avant demi |
| +100 | 180° | Marche avant max |

#### Valeurs de Yaw

| Valeur | Servo Angle | État |
|--------|-------------|------|
| -100 | 0° | Rotation gauche max |
| -50 | 45° | Rotation gauche demi |
| 0 | 90° | Pas de rotation |
| +50 | 135° | Rotation droite demi |
| +100 | 180° | Rotation droite max |

#### Exemple : Marche Avant

```
Throttle : +100  (Avance)
Yaw      : 0     (Droit)
Pitch    : 0
Roll     : 0

Payload : [0x64, 0x00, 0x00, 0x00] (hexadécimal)
         ou
Payload : [100, 0, 0, 0] (décimal)
```

#### Exemple : Rotation Gauche

```
Throttle : 0     (Arrêt)
Yaw      : -100  (Gauche)
Pitch    : 0
Roll     : 0

Payload : [0x00, 0x9C, 0x00, 0x00]
```

#### Exemple : Marche Avant + Rotation Droite

```
Throttle : +80   (Avance)
Yaw      : +50   (Droit)
Pitch    : 0
Roll     : 0

Payload : [0x50, 0x32, 0x00, 0x00]
```

### Implémentation Kotlin (Android)

```kotlin
fun sendCommand(throttle: Int, yaw: Int, pitch: Int = 0, roll: Int = 0) {
    val payload = byteArrayOf(
        throttle.coerceIn(-100, 100).toByte(),
        yaw.coerceIn(-100, 100).toByte(),
        pitch.coerceIn(-100, 100).toByte(),
        roll.coerceIn(-100, 100).toByte()
    )
    
    characteristic.value = payload
    gatt?.writeCharacteristic(characteristic)
}
```

### Implémentation C++ (ESP32)

```cpp
void onWrite(BLECharacteristic *pCharacteristic) {
    std::string value = pCharacteristic->getValue();
    
    if (value.length() == 4) {
        int8_t throttle = (int8_t)value[0];
        int8_t yaw      = (int8_t)value[1];
        int8_t pitch    = (int8_t)value[2];
        int8_t roll     = (int8_t)value[3];
        
        // Convertir en angle (0-180)
        int throttleAngle = map(throttle, -100, 100, 0, 180);
        int yawAngle      = map(yaw, -100, 100, 0, 180);
        
        servoThrottle.write(throttleAngle);
        servoYaw.write(yawAngle);
        
        Serial.printf("T:%d Y:%d\n", throttle, yaw);
    }
}
```

---

## Service Batterie (Standard BLE)

**UUID Service** : `180f` (Standard Battery Service)  
**UUID Caractéristique** : `2a19` (Battery Level)

### Format

```
Format: 1 byte non signé (uint8)
Valeur: 0-100 (en pourcentage)
Propriétés: READ, NOTIFY
```

### Lecture Simple

```kotlin
fun readBatteryLevel() {
    val batteryService = gatt?.getService(UUID.fromString("180f"))
    val batteryChar = batteryService?.getCharacteristic(
        UUID.fromString("2a19")
    )
    gatt?.readCharacteristic(batteryChar)
}

// Callback
override fun onCharacteristicRead(
    gatt: BluetoothGatt,
    characteristic: BluetoothGattCharacteristic,
    status: Int
) {
    if (characteristic.uuid.toString() == "2a19") {
        val batteryLevel = characteristic.value[0].toInt() and 0xFF
        // batteryLevel est maintenant entre 0 et 100
    }
}
```

### Notification (Lecture Continue)

```kotlin
fun enableBatteryNotifications() {
    val batteryChar = gatt?.getService(UUID.fromString("180f"))
        ?.getCharacteristic(UUID.fromString("2a19"))
    
    // Enable notifications
    gatt?.setCharacteristicNotification(batteryChar, true)
    
    // Configure descriptor
    val descriptor = batteryChar?.getDescriptor(
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    )
    descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
    gatt?.writeDescriptor(descriptor)
}

// Callback de notification
override fun onCharacteristicChanged(
    gatt: BluetoothGatt,
    characteristic: BluetoothGattCharacteristic
) {
    if (characteristic.uuid.toString() == "2a19") {
        val batteryLevel = characteristic.value[0].toInt() and 0xFF
        updateBatteryUI(batteryLevel)
    }
}
```

### Calcul sur ESP32

```cpp
void updateBatteryLevel() {
    int rawValue = analogRead(BATTERY_PIN);
    
    // Étalonnage : adapter CALIB_MIN et CALIB_MAX
    // avec vos valeurs mesurées
    #define CALIB_MIN 1800  // ADC à batterie vide
    #define CALIB_MAX 4095  // ADC à batterie pleine
    
    int batteryPercent = map(rawValue, CALIB_MIN, CALIB_MAX, 0, 100);
    batteryPercent = constrain(batteryPercent, 0, 100);
    
    uint8_t batteryData = (uint8_t)batteryPercent;
    pBatteryChar->setValue(&batteryData, 1);
    pBatteryChar->notify();
}
```

---

## Connexion / Déconnexion

### Connexion

1. Client (Android) scrute les appareils BLE
2. Reçoit l'annonce de `ESP32_RC_CAR`
3. Initialise la connexion
4. Découvre les services (GATT)
5. Établit les caractéristiques
6. Prêt à envoyer des commandes

### Déconnexion

La déconnexion peut être :
- **Volontaire** : Appui sur bouton "Déconnecter"
- **Involontaire** : Batterie faible, interférence, sortie de portée

Après déconnexion :
- L'ESP32 accepte à nouveau les connexions
- L'application relance automatiquement la recherche

---

## Protocole de Communication - Timing

### Fréquence d'Envoi Recommandée

```
Fréquence: 10 Hz (100 ms entre chaque envoi)
Latence: 50-150 ms typiquement
Fiabilité: >99% en conditions normales
```

### Optimisation

```kotlin
// Ne pas envoyer plus souvent
private val sendCommandDebounce = 100 // ms

private var lastCommandTime = 0L

fun throttledSendCommand(throttle: Int, yaw: Int) {
    val now = System.currentTimeMillis()
    if (now - lastCommandTime >= sendCommandDebounce) {
        sendCommand(throttle, yaw)
        lastCommandTime = now
    }
}
```

---

## Gestion des Erreurs

### Code d'État BLE

| Code | Signification | Action |
|------|---|---|
| 0 | GATT_SUCCESS | ✓ Succès |
| 2 | GATT_READ_NOT_PERMITTED | Lire non autorisé |
| 3 | GATT_WRITE_NOT_PERMITTED | Écrire non autorisé |
| 8 | GATT_REQUEST_NOT_SUPPORTED | Non supporté |
| 15 | GATT_INSUFFICIENT_ENCRYPTION | Encryption insuffisante |

### Reconnexion Automatique

```kotlin
fun reconnectOnFailure(gatt: BluetoothGatt) {
    Handler(Looper.getMainLooper()).postDelayed({
        if (!isConnected) {
            bluetoothDevice.connectGatt(context, true, gattCallback)
        }
    }, 2000) // Attendre 2 secondes
}
```

---

## Sécurité

### Portée BLE

- **Théorique** : 240 mètres (ligne de vue)
- **Pratique** : 10-30 mètres à travers les murs

### Interférences

Les appareils suivants peuvent interférer :
- WiFi 2.4 GHz
- Fours à micro-ondes
- Appareils Bluetooth proches

### Données Non Chiffrées

ℹ️ Note : Ce protocole ne chiffre **pas** les données. Convient pour un usage privé, **ne pas utiliser** en environnement public hostile.

---

## Débogage

### Moniteur Série ESP32

```cpp
Serial.println("=== BLE Command Received ===");
Serial.printf("Throttle: %d, Yaw: %d\n", throttle, yaw);
Serial.printf("Servo Angles: T=%d°, Y=%d°\n", throttleAngle, yawAngle);
```

### Logs BLE Android

```kotlin
Log.d("BLE", "Connected to device")
Log.d("BLE", "Services discovered: ${gatt.services.size}")
Log.d("BLE", "Sending command: [${throttle}, ${yaw}, 0, 0]")
Log.e("BLE", "Write failed with status: $status")
```

---

**Fin de la documentation API** ✓
