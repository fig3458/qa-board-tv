# Keep WebView bridge if used later
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
