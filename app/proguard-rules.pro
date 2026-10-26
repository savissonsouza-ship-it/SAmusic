# Moshi / Retrofit
-keep class com.sonora.app.data.remote.dto.** { *; }
-keepclassmembers class * { @com.squareup.moshi.Json <fields>; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**
