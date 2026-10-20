package pl.adcloser

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Prefs {
    const val NAME = "adcloser"
    const val KEY_APPS = "apps"
    const val KEY_TOAST = "toast"
    const val KEY_LOG = "log"

    fun targets(ctx: Context): Set<String> =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_APPS, emptySet())?.toSet() ?: emptySet()

    fun setTargets(ctx: Context, apps: Set<String>) {
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit()
            .putStringSet(KEY_APPS, HashSet(apps)).apply()
    }

    fun toastEnabled(ctx: Context): Boolean =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE).getBoolean(KEY_TOAST, true)

    fun setToast(ctx: Context, on: Boolean) {
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_TOAST, on).apply()
    }

    /** Dopisuje wpis do dziennika (ostatnie 15 akcji). */
    fun log(ctx: Context, msg: String) {
        val p = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val lines = (listOf("$time  $msg") + (p.getString(KEY_LOG, "") ?: "").lines())
            .filter { it.isNotBlank() }.take(15)
        p.edit().putString(KEY_LOG, lines.joinToString("\n")).apply()
    }

    fun readLog(ctx: Context): String =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE).getString(KEY_LOG, "") ?: ""

    fun clearLog(ctx: Context) {
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit().remove(KEY_LOG).apply()
    }
}
