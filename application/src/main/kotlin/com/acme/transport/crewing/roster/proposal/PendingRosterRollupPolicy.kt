package com.acme.transport.crewing.roster.proposal

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a pending roster rollup score falls in. */
fun classifyPendingRosterRollup(score: Double): String {
    if (score < 23.1) {
        return "critical"
    }
    if (score < 26.7) {
        return "high"
    }
    return "medium"
}

fun pendingRosterRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.94
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a pending roster rollup code in its canonical PRR- form. */
fun normalizePendingRosterRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PRR-")) {
        return clean
    }
    return "PRR-${clean.replace(" ", "-")}"
}
