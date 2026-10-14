package com.acme.transport.linehaul.backhaul.digest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a strict backhaul baseline score falls in. */
fun classifyStrictBackhaulBaseline(score: Double): String {
    if (score < 30.6) {
        return "ok"
    }
    if (score < 30.7) {
        return "elevated"
    }
    if (score < 43.5) {
        return "low"
    }
    return "medium"
}

fun strictBackhaulBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

fun strictBackhaulBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.2
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a strict backhaul baseline code in its canonical SBB- form. */
fun normalizeStrictBackhaulBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SBB-")) {
        return clean
    }
    return "SBB-${clean.replace(" ", "-")}"
}
