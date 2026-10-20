package pl.adcloser

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var logView: TextView
    private var apps: List<Pair<String, String>> = emptyList() // (nazwa, pakiet)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (16 * resources.displayMetrics.density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        status = TextView(this).apply { textSize = 18f; setTypeface(typeface, Typeface.BOLD) }
        root.addView(status)

        root.addView(Button(this).apply {
            text = "Włącz usługę (ustawienia ułatwień dostępu)"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })

        root.addView(Switch(this).apply {
            text = "Pokazuj komunikat po zamknięciu reklamy"
            isChecked = Prefs.toastEnabled(this@MainActivity)
            setOnCheckedChangeListener { _, on -> Prefs.setToast(this@MainActivity, on) }
            setPadding(0, pad / 2, 0, pad / 2)
        })

        root.addView(TextView(this).apply {
            text = "Ostatnie akcje (dotknij, aby wyczyścić):"
            setTypeface(typeface, Typeface.BOLD)
        })
        logView = TextView(this).apply {
            textSize = 12f
            maxLines = 6
            setPadding(0, 0, 0, pad / 2)
            setOnClickListener { Prefs.clearLog(this@MainActivity); refresh() }
        }
        root.addView(logView)

        root.addView(TextView(this).apply {
            text = "Zaznacz aplikacje, w których zamykać reklamy:"
            setTypeface(typeface, Typeface.BOLD)
        })

        val list = ListView(this).apply { choiceMode = ListView.CHOICE_MODE_MULTIPLE }
        root.addView(list, LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f))
        setContentView(root, android.view.ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))

        loadApps(list)
    }

    private fun loadApps(list: ListView) {
        val selected = Prefs.targets(this)
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        apps = packageManager.queryIntentActivities(intent, 0)
            .map { it.loadLabel(packageManager).toString() to it.activityInfo.packageName }
            .filter { it.second != packageName }
            .distinctBy { it.second }
            .sortedWith(compareBy({ it.second !in selected }, { it.first.lowercase() }))

        list.adapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_multiple_choice,
            apps.map { it.first }
        )
        apps.forEachIndexed { i, app -> list.setItemChecked(i, app.second in selected) }

        list.setOnItemClickListener { _, _, _, _ ->
            val checked = list.checkedItemPositions
            val chosen = apps.indices.filter { checked.get(it) }.map { apps[it].second }.toSet()
            Prefs.setTargets(this, chosen)
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val enabled = (Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: "").contains("$packageName/")
        status.text = if (enabled) "✅ Usługa działa" else "❌ Usługa wyłączona"
        logView.text = Prefs.readLog(this).ifBlank { "(brak)" }
    }
}
