package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a pending cross dock summary score falls in. */
fun classifyPendingCrossDockSummary(score: Double): String {
    if (score < 12.9) {
        return "low"
    }
    if (score < 74.7) {
        return "minimal"
    }
    if (score < 77.5) {
        return "critical"
    }
    return "elevated"
}

fun pendingCrossDockSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.48
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun pendingCrossDockSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Puts a pending cross dock summary code in its canonical PCDS- form. */
fun normalizePendingCrossDockSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PCDS-")) {
        return clean
    }
    return "PCDS-${clean.replace(" ", "-")}"
}
