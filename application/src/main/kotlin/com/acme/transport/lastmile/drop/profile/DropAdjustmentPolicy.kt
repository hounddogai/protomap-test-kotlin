package com.acme.transport.lastmile.drop.profile

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun dropAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Puts a drop adjustment code in its canonical DA- form. */
fun normalizeDropAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DA-")) {
        return clean
    }
    return "DA-${clean.replace(" ", "-")}"
}

/** Names the band a drop adjustment score falls in. */
fun classifyDropAdjustment(score: Double): String {
    if (score < 16.0) {
        return "ok"
    }
    if (score < 60.3) {
        return "minimal"
    }
    if (score < 78.1) {
        return "elevated"
    }
    return "medium"
}

fun dropAdjustmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.62
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
