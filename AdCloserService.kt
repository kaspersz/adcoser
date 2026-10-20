package pl.adcloser

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

/**
 * Usługa ułatwień dostępu, która w wybranych aplikacjach szuka przycisku
 * zamknięcia reklamy (X / Zamknij / Close / Skip) i go klika.
 */
class AdCloserService : AccessibilityService() {

    private var lastScan = 0L
    private var lastClick = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        val targets = Prefs.targets(this)
        if (pkg !in targets) return

        val now = SystemClock.uptimeMillis()
        // Nie sprawdzamy częściej niż co 500 ms, a po kliknięciu czekamy 1,5 s
        if (now - lastScan < SCAN_INTERVAL_MS || now - lastClick < CLICK_COOLDOWN_MS) return
        lastScan = now

        scan(targets)
    }

    private fun scan(targets: Set<String>) {
        val roots = try {
            windows.mapNotNull { it.root }
        } catch (e: Exception) {
            emptyList()
        }.ifEmpty { listOfNotNull(rootInActiveWindow) }

        for (root in roots) {
            val rootPkg = root.packageName?.toString() ?: continue
            if (rootPkg !in targets) continue
            val node = findClose(root, 0) ?: continue
            val label = describe(node)
            if (click(node)) {
                lastClick = SystemClock.uptimeMillis()
                Prefs.log(this, "Kliknięto „$label” w $rootPkg")
                if (Prefs.toastEnabled(this)) {
                    Toast.makeText(this, "AdCloser: zamknięto reklamę", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    /** Przeszukuje drzewo widoków w głąb i zwraca pierwszy pasujący element. */
    private fun findClose(node: AccessibilityNodeInfo, depth: Int): AccessibilityNodeInfo? {
        if (depth > MAX_DEPTH) return null
        if (isCloseButton(node)) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findClose(child, depth + 1)
            if (found != null) return found
        }
        return null
    }

    private fun isCloseButton(n: AccessibilityNodeInfo): Boolean {
        if (!n.isVisibleToUser) return false
        val desc = n.contentDescription?.toString()?.trim()?.lowercase() ?: ""
        val text = n.text?.toString()?.trim()?.lowercase() ?: ""
        val id = n.viewIdResourceName?.substringAfter(":id/")?.lowercase() ?: ""
        return matchesLabel(desc) || matchesLabel(text) || ID_KEYWORDS.any { it in id }
    }

    private fun matchesLabel(s: String): Boolean {
        if (s.isEmpty() || s.length > 30) return false
        if (s in EXACT_LABELS) return true
        val words = s.split(Regex("[^\\p{L}]+")).filter { it.isNotEmpty() }
        return words.any { it in KEYWORDS }
    }

    private fun click(node: AccessibilityNodeInfo): Boolean {
        if (node.isClickable && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
        // Jeśli sam element nie jest klikalny, „stukamy” palcem w jego środek
        return tap(node)
    }

    private fun tap(node: AccessibilityNodeInfo): Boolean {
        val r = Rect()
        node.getBoundsInScreen(r)
        if (r.isEmpty) return false
        val path = Path().apply { moveTo(r.exactCenterX(), r.exactCenterY()) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 60)
        return dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
    }

    private fun describe(n: AccessibilityNodeInfo): String =
        n.contentDescription?.toString()?.takeIf { it.isNotBlank() }
            ?: n.text?.toString()?.takeIf { it.isNotBlank() }
            ?: n.viewIdResourceName?.substringAfter(":id/")
            ?: "?"

    override fun onInterrupt() {}

    companion object {
        private const val SCAN_INTERVAL_MS = 500L
        private const val CLICK_COOLDOWN_MS = 1500L
        private const val MAX_DEPTH = 60

        private val EXACT_LABELS = setOf("x", "×", "✕", "✖", "✗", "╳", "ⓧ", "⊗")
        private val KEYWORDS = setOf(
            "close", "zamknij", "skip", "pomiń", "dismiss", "zamknąć"
        )
        private val ID_KEYWORDS = listOf("close", "dismiss", "skip")
    }
}
