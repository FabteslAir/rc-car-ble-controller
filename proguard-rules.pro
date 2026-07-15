
# ProGuard rules for RC Car Controller

# Preserve all public classes and their public members
-keep public class * {
    public *;
}

# Preserve all line numbers for stack traces
-keepattributes SourceFile,LineNumberTable

# Rename obfuscated stack traces
-renamesourcefileattribute SourceFile

# Kotlin
-keepclassmembers class **$WhenMapped {
    *** instance;
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# Androidx
-keep class androidx.** {*;}
-keep interface androidx.** {*;}

# Compose
-keep class androidx.compose.** {*;}
-keep interface androidx.compose.** {*;}

# Material3
-keep class com.google.android.material.** {*;}
-keep interface com.google.android.material.** {*;}

# Bluetooth
-keep class android.bluetooth.** {*;}
-keep interface android.bluetooth.** {*;}

# Coroutines
-keep class kotlinx.coroutines.** {*;}
-keep interface kotlinx.coroutines.** {*;}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Debugging
-verbose
-printmapping build/outputs/mapping/release/mapping.txt
