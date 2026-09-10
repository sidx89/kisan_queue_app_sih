# KisanProcure Proguard Rules

-keep class com.kisanprocure.app.data.model.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Socket.IO
-keep class io.socket.** { *; }
-keep class okhttp3.** { *; }

# Firebase
-dontwarn com.google.firebase.**
-keep class com.google.firebase.** { *; }
