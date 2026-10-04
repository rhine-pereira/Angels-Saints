# Retrofit
-if interface *
-keepclasseswithmembers,allowobfuscation interface <1> {
    @retrofit2.http.* <methods>;
}
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# Moshi
-keep @com.squareup.moshi.JsonQualifier interface *
-keepclassmembers class ** {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}
-keepclassmembers class ** {
    @com.squareup.moshi.Json <fields>;
}

# KotlinJsonAdapterFactory needs metadata and generic signatures in release builds.
-keep class kotlin.Metadata { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Network models and custom Moshi adapters used by Retrofit.
-keep class com.rhinepereira.saints.data.remote.** { *; }
-keep class com.rhinepereira.saints.data.model.** { *; }
