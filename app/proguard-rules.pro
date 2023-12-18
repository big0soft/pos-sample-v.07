# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:/adt-bundle-windows-x86_64/adt-bundle-windows-x86_64/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#    -optimizationpasses 5
#
#    -dontusemixedcaseclassnames
#
#    -dontskipnonpubliclibraryclasses
#
#    -dontoptimize
#
#    -dontpreverify
#
#     #log
#    -verbose
#
#     # proguard method cfg
#    -optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#
#    #keep Annotation
#    -keepattributes *Annotation*
#
#    -keep public class * extends android.app.Fragment
#    -keep public class * extends android.app.Activity
#    -keep public class * extends android.app.Application
#    -keep public class * extends android.app.Service
#    -keep public class * extends android.content.BroadcastReceiver
#    -keep public class * extends android.content.ContentProvider
#    -keep public class * extends android.app.backup.BackupAgentHelper
#    -keep public class * extends android.preference.Preference
#    -keep public class com.android.vending.licensing.ILicensingService
#    -keep public class * extends android.support.v4.app.Fragment
#
#
#
#    -ignorewarning
#    -dump class_files.txt
#    -printseeds seeds.txt
#    -printusage unused.txt
#    -printmapping mapping.txt
#
#    #if use v4 or v7 lib
#    -dontwarn android.support.**
#
#    ####keep jar library-end####
#
#    -keep public class * extends android.view.View {
#        public <init>(android.content.Context);
#        public <init>(android.content.Context, android.util.AttributeSet);
#        public <init>(android.content.Context, android.util.AttributeSet, int);
#        public void set*(...);
#    }
#
#    #keep native
#    -keepclasseswithmembernames class * {
#        native <methods>;
#    }
#
#    #keep custom view
#    -keepclasseswithmembers class * {
#        public <init>(android.content.Context, android.util.AttributeSet);
#    }
#
#    #keep custom view
#    -keepclassmembers class * extends android.app.Activity {
#       public void *(android.view.View);
#    }
#
#    #keep Parcelable
#    -keep class * implements android.os.Parcelable {
#      public static final android.os.Parcelable$Creator *;
#    }
#
#    #keep Serializable
#    -keepnames class * implements java.io.Serializable
#
#    #keep Serializable
#    -keepclassmembers class * implements java.io.Serializable {
#        static final long serialVersionUID;
#        private static final java.io.ObjectStreamField[] serialPersistentFields;
#        !static !transient <fields>;
#        !private <fields>;
#        !private <methods>;
#        private void writeObject(java.io.ObjectOutputStream);
#        private void readObject(java.io.ObjectInputStream);
#        java.lang.Object writeReplace();
#        java.lang.Object readResolve();
#    }
#
#    #keep enum
#    #-keepclassmembers enum * {
#    #  public static **[] values();
#    #  public static ** valueOf(java.lang.String);
#    #}
#
#    -keepclassmembers class * {
#        public void *ButtonClicked(android.view.View);
#    }
#
#    #keep resouces
#    -keepclassmembers class **.R$* {
#        public static <fields>;
#    }
#
##eventbus
#    -keepclassmembers class ** {
#        public void onEvent*(***);
#    }
#    # Only required if you use AsyncExecutor
#    -keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
#        <init>(java.lang.Throwable);
#    }
## rxjava
#    -keep class rx.schedulers.Schedulers {
#        public static <methods>;
#    }
#    -keep class rx.schedulers.ImmediateScheduler {
#        public <methods>;
#    }
#    -keep class rx.schedulers.TestScheduler {
#        public <methods>;
#    }
#    -keep class rx.schedulers.Schedulers {
#        public static ** test();
#    }
#    -keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
#        long producerIndex;
#        long consumerIndex;
#    }
#    -keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
#        long producerNode;
#        long consumerNode;
#    }
##ddi
#    -keep class com.xinguodu.ddiinterface.**{*;}
#    #SmartPOSJni
#        -keep class com.nexgo.oaf.smartpos.jni.SmartPOSJni{*;}
#    #API
#        -keep class com.nexgo.oaf.smartpos.CardAPI {*;}
#        -keep class com.nexgo.oaf.smartpos.DeviceAPI {*;}
#        -keep class com.nexgo.oaf.smartpos.KeyAPI {*;}
#        -keep class com.nexgo.oaf.smartpos.OtherAPI {*;}
#        -keep class com.nexgo.oaf.smartpos.PeripheralAPI {*;}
#    #DeviceEngineImpl
#        -keep class com.nexgo.oaf.smartpos.apiv3.DeviceEngineImpl {*;}
#
