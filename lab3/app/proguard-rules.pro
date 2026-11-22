# Прибираємо виклики android.util.Log з release-збірки (Timber у release логує через ReleaseTree).
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
