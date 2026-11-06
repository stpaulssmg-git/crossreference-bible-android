# R8 / Proguard Rules for Apostolic Reference Bible

# 1. Keep JavaScript Interface methods for WebView bridge
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keepclassmembers class com.stpauls.apostolicreferencebible.MainActivity$CrossReferenceInterface {
    <methods>;
}

# 2. Keep Data Models for SQLite, JSON serialization, and reflection
-keep class com.stpauls.apostolicreferencebible.db.** { *; }

# 3. Preserve line numbers and source files for crash reporting
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# 4. Suppress non-critical Proguard warnings
-dontwarn android.webkit.**
