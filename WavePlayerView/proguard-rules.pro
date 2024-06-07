-keep class sound.wave.kilobyte.** { *; }
-keep class * extends android.app.Activity { *; }
-keep class * extends android.app.Service { *; }
-keep class * extends android.app.IntentService { *; }
-keep class * extends android.content.BroadcastReceiver { *; }
-keep class * extends android.content.ContentProvider { *; }
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-obfuscationdictionary proguard-dictionaries/obfuscation-dictionary.txt
-classobfuscationdictionary proguard-dictionaries/class-obfuscation-dictionary.txt
-packageobfuscationdictionary proguard-dictionaries/package-obfuscation-dictionary.txt

-dontshrink
-dontoptimize

-printusage usage.txt

-dontnote
-dontwarn

-keepattributes *Annotation*
-keepattributes *EnclosingMethod*
-keepattributes *Signature*
-keepattributes *InnerClasses*
-keepattributes SourceFile,LineNumberTable