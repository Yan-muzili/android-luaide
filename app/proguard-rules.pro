# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.preference.Preference
-keep public class * extends android.preference.PreferenceActivity
-keep public class * extends android.accessibilityservice.AccessibilityService
-keep class com.yan.luaeditor.** { *; }
-keep class com.yan.luaide.** { *; }
-keep class androidx.appcompat.** { *; }
-keep public class * extends androidx.appcompat.**
-keep class androidx.fragment.** { *; }
-keep public class * extends androidx.fragment.**
-keep class io.github.rosemoe.sora.langs.** { *; }
-keep public class * extends io.github.rosemoe.sora.langs.**
-keep class org.eclipse.tm4e.languageconfiguration.internal.model.** { *; }
-keep public class * extends org.eclipse.tm4e.languageconfiguration.internal.model.**