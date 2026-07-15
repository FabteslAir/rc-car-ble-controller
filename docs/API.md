\# Documentation API - Protocole BLE 📡



\## Vue d'ensemble



Le système utilise \*\*Bluetooth Low Energy (BLE)\*\* pour la communication bidirectionnelle entre l'application Android et l'ESP32.



\---



\## Architecture BLE



\### Services et Caractéristiques



```

Device: ESP32\_RC\_CAR

│

├── Service: RC Control (4fafc201-1fb5-459e-8fcc-c5c9c331914b)

│   └── Characteristic: Command (beb5483b-36e1-4688-b7f5-ea07361b26a8)

│       Properties: READ, WRITE

│       Format: 4 bytes

│

└── Service: Battery (180f) \[Standard]

&#x20;   └── Characteristic: Level (2a19)

&#x20;       Properties: READ, NOTIFY

&#x20;       Format: 1 byte (0-100%)

```



\---



\## Service de Contrôle



\### Commande de Mouvement



\*\*UUID Service\*\* : `4fafc201-1fb5-459e-8fcc-c5c9c331914b`  

\*\*UUID Caractéristique\*\* : `beb5483b-36e1-4688-b7f5-ea07361b26a8`



\#### Format du Payload



```

Byte 0 : Throttle    (int8)  : -100 à +100

Byte 1 : Yaw         (int8)  : -100 à +100

Byte 2 : Pitch       (int8)  : -100 à +100 (réservé)

Byte 3 : Roll        (int8)  : -100 à +100 (réservé)

```



\#### Valeurs de Throttle



|

&#x20;Valeur 

|

&#x20;Servo Angle 

|

&#x20;État 

|

|

\--------

|

\-------------

|

\------

|

|

&#x20;-100 

|

&#x20;0° 

|

&#x20;Marche arrière max 

|

|

&#x20;-50 

|

&#x20;45° 

|

&#x20;Marche arrière demi 

|

|

&#x20;0 

|

&#x20;90° 

|

&#x20;Arrêt / Centre 

|

|

&#x20;+50 

|

&#x20;135° 

|

&#x20;Marche avant demi 

|

|

&#x20;+100 

|

&#x20;180° 

|

&#x20;Marche avant max 

|



\#### Valeurs de Yaw



|

&#x20;Valeur 

|

&#x20;Servo Angle 

|

&#x20;État 

|

|

\--------

|

\-------------

|

\------

|

|

&#x20;-100 

|

&#x20;0° 

|

&#x20;Rotation gauche max 

|

|

&#x20;-50 

|

&#x20;45° 

|

&#x20;Rotation gauche demi 

|

|

&#x20;0 

|

&#x20;90° 

|

&#x20;Pas de rotation 

|

|

&#x20;+50 

|

&#x20;135° 

|

&#x20;Rotation droite demi 

|

|

&#x20;+100 

|

&#x20;180° 

|

&#x20;Rotation droite max 

|



\#### Exemple : Marche Avant



```

Throttle : +100  (Avance)

Yaw      : 0     (Droit)

Pitch    : 0

Roll     : 0



Payload : \[0x64, 0x00, 0x00, 0x00] (hexadécimal)

&#x20;        ou

Payload : \[100, 0, 0, 0] (décimal)

```



\#### Exemple : Rotation Gauche



```

Throttle : 0     (Arrêt)

Yaw      : -100  (Gauche)

Pitch    : 0

Roll     : 0



Payload : \[0x00, 0x9C, 0x00, 0x00]

```



\#### Exemple : Marche Avant + Rotation Droite



```

Throttle : +80   (Avance)

Yaw      : +50   (Droit)

Pitch    : 0

Roll     : 0



Payload : \[0x50, 0x32, 0x00, 0x00]

```



\### Implémentation Kotlin (Android)



```kotlin

fun sendCommand(throttle: Int, yaw: Int, pitch: Int = 0, roll: Int = 0) {

&#x20;   val payload = byteArrayOf(

&#x20;       throttle.coerceIn(-100, 100).toByte(),

&#x20;       yaw.coerceIn(-100, 100).toByte(),

&#x20;       pitch.coerceIn(-100, 100).toByte(),

&#x20;       roll.coerceIn(-100, 100).toByte()

&#x20;   )

&#x20;   

&#x20;   characteristic.value = payload

&#x20;   gatt?.writeCharacteristic(characteristic)

}

```



\### Implémentation C++ (ESP32)



```cpp

void onWrite(BLECharacteristic \*pCharacteristic) {

&#x20;   std::string value = pCharacteristic->getValue();

&#x20;   

&#x20;   if (value.length() == 4) {

&#x20;       int8\_t throttle = (int8\_t)value\[0];

&#x20;       int8\_t yaw      = (int8\_t)value\[1];

&#x20;       int8\_t pitch    = (int8\_t)value\[2];

&#x20;       int8\_t roll     = (int8\_t)value\[3];

&#x20;       

&#x20;       // Convertir en angle (0-180)

&#x20;       int throttleAngle = map(throttle, -100, 100, 0, 180);

&#x20;       int yawAngle      = map(yaw, -100, 100, 0, 180);

&#x20;       

&#x20;       servoThrottle.write(throttleAngle);

&#x20;       servoYaw.write(yawAngle);

&#x20;       

&#x20;       Serial.printf("T:%d Y:%d\\n", throttle, yaw);

&#x20;   }

}

```



\---



\## Service Batterie (Standard BLE)



\*\*UUID Service\*\* : `180f` (Standard Battery Service)  

\*\*UUID Caractéristique\*\* : `2a19` (Battery Level)



\### Format



```

Format: 1 byte non signé (uint8)

Valeur: 0-100 (en pourcentage)

Propriétés: READ, NOTIFY

```



\### Lecture Simple



```kotlin

fun readBatteryLevel() {

&#x20;   val batteryService = gatt?.getService(UUID.fromString("180f"))

&#x20;   val batteryChar = batteryService?.getCharacteristic(

&#x20;       UUID.fromString("2a19")

&#x20;   )

&#x20;   gatt?.readCharacteristic(batteryChar)

}



// Callback

override fun onCharacteristicRead(

&#x20;   gatt: BluetoothGatt,

&#x20;   characteristic: BluetoothGattCharacteristic,

&#x20;   status: Int

) {

&#x20;   if (characteristic.uuid.toString() == "2a19") {

&#x20;       val batteryLevel = characteristic.value\[0].toInt() and 0xFF

&#x20;       // batteryLevel est maintenant entre 0 et 100

&#x20;   }

}

```



\### Notification (Lecture Continue)



```kotlin

fun enableBatteryNotifications() {

&#x20;   val batteryChar = gatt?.getService(UUID.fromString("180f"))

&#x20;       ?.getCharacteristic(UUID.fromString("2a19"))

&#x20;   

&#x20;   // Enable notifications

&#x20;   gatt?.setCharacteristicNotification(batteryChar, true)

&#x20;   

&#x20;   // Configure descriptor

&#x20;   val descriptor = batteryChar?.getDescriptor(

&#x20;       UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

&#x20;   )

&#x20;   descriptor?.value = BluetoothGattDescriptor.ENABLE\_NOTIFICATION\_VALUE

&#x20;   gatt?.writeDescriptor(descriptor)

}



// Callback de notification

override fun onCharacteristicChanged(

&#x20;   gatt: BluetoothGatt,

&#x20;   characteristic: BluetoothGattCharacteristic

) {

&#x20;   if (characteristic.uuid.toString() == "2a19") {

&#x20;       val batteryLevel = characteristic.value\[0].toInt() and 0xFF

&#x20;       updateBatteryUI(batteryLevel)

&#x20;   }

}

```



\### Calcul sur ESP32



```cpp

void updateBatteryLevel() {

&#x20;   int rawValue = analogRead(BATTERY\_PIN);

&#x20;   

&#x20;   // Étalonnage : adapter CALIB\_MIN et CALIB\_MAX

&#x20;   // avec vos valeurs mesurées

&#x20;   #define CALIB\_MIN 1800  // ADC à batterie vide

&#x20;   #define CALIB\_MAX 4095  // ADC à batterie pleine

&#x20;   

&#x20;   int batteryPercent = map(rawValue, CALIB\_MIN, CALIB\_MAX, 0, 100);

&#x20;   batteryPercent = constrain(batteryPercent, 0, 100);

&#x20;   

&#x20;   uint8\_t batteryData = (uint8\_t)batteryPercent;

&#x20;   pBatteryChar->setValue(\&batteryData, 1);

&#x20;   pBatteryChar->notify();

}

```



\---



\## Connexion / Déconnexion



\### Connexion



1\. Client (Android) scrute les appareils BLE

2\. Reçoit l'annonce de `ESP32\_RC\_CAR`

3\. Initialise la connexion

4\. Découvre les services (GATT)

5\. Établit les caractéristiques

6\. Prêt à envoyer des commandes



\### Déconnexion



La déconnexion peut être :

\- \*\*Volontaire\*\* : Appui sur bouton "Déconnecter"

\- \*\*Involontaire\*\* : Batterie faible, interférence, sortie de portée



Après déconnexion :

\- L'ESP32 accepte à nouveau les connexions

\- L'application relance automatiquement la recherche



\---



\## Protocole de Communication - Timing



\### Fréquence d'Envoi Recommandée



```

Fréquence: 10 Hz (100 ms entre chaque envoi)

Latence: 50-150 ms typiquement

Fiabilité: >99% en conditions normales

```



\### Optimisation



```kotlin

// Ne pas envoyer plus souvent

private val sendCommandDebounce = 100 // ms



private var lastCommandTime = 0L



fun throttledSendCommand(throttle: Int, yaw: Int) {

&#x20;   val now = System.currentTimeMillis()

&#x20;   if (now - lastCommandTime >= sendCommandDebounce) {

&#x20;       sendCommand(throttle, yaw)

&#x20;       lastCommandTime = now

&#x20;   }

}

```



\---



\## Gestion des Erreurs



\### Code d'État BLE



|

&#x20;Code 

|

&#x20;Signification 

|

&#x20;Action 

|

|

\------

|

\---

|

\---

|

|

&#x20;0 

|

&#x20;GATT\_SUCCESS 

|

&#x20;✓ Succès 

|

|

&#x20;2 

|

&#x20;GATT\_READ\_NOT\_PERMITTED 

|

&#x20;Lire non autorisé 

|

|

&#x20;3 

|

&#x20;GATT\_WRITE\_NOT\_PERMITTED 

|

&#x20;Écrire non autorisé 

|

|

&#x20;8 

|

&#x20;GATT\_REQUEST\_NOT\_SUPPORTED 

|

&#x20;Non supporté 

|

|

&#x20;15 

|

&#x20;GATT\_INSUFFICIENT\_ENCRYPTION 

|

&#x20;Encryption insuffisante 

|



\### Reconnexion Automatique



```kotlin

fun reconnectOnFailure(gatt: BluetoothGatt) {

&#x20;   Handler(Looper.getMainLooper()).postDelayed({

&#x20;       if (!isConnected) {

&#x20;           bluetoothDevice.connectGatt(context, true, gattCallback)

&#x20;       }

&#x20;   }, 2000) // Attendre 2 secondes

}

```



\---



\## Sécurité



\### Portée BLE



\- \*\*Théorique\*\* : 240 mètres (ligne de vue)

\- \*\*Pratique\*\* : 10-30 mètres à travers les murs



\### Interférences



Les appareils suivants peuvent interférer :

\- WiFi 2.4 GHz

\- Fours à micro-ondes

\- Appareils Bluetooth proches



\### Données Non Chiffrées



ℹ️ Note : Ce protocole ne chiffre \*\*pas\*\* les données. Convient pour un usage privé, \*\*ne pas utiliser\*\* en environnement public hostile.



\---



\## Débogage



\### Moniteur Série ESP32



```cpp

Serial.println("=== BLE Command Received ===");

Serial.printf("Throttle: %d, Yaw: %d\\n", throttle, yaw);

Serial.printf("Servo Angles: T=%d°, Y=%d°\\n", throttleAngle, yawAngle);

```



\### Logs BLE Android



```kotlin

Log.d("BLE", "Connected to device")

Log.d("BLE", "Services discovered: ${gatt.services.size}")

Log.d("BLE", "Sending command: \[${throttle}, ${yaw}, 0, 0]")

Log.e("BLE", "Write failed with status: $status")

```



\---



\*\*Fin de la documentation API\*\* ✓



