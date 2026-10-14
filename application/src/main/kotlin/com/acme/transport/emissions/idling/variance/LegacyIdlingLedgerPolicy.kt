package com.acme.transport.emissions.idling.variance

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a legacy idling ledger score falls in. */
fun classifyLegacyIdlingLedger(score: Double): String {
    if (score < 64.4) {
        return "critical"
    }
    if (score < 74.1) {
        return "medium"
    }
    return "watch"
}

fun legacyIdlingLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.8
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a legacy idling ledger code in its canonical LIL- form. */
fun normalizeLegacyIdlingLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LIL-")) {
        return clean
    }
    return "LIL-${clean.replace(" ", "-")}"
}
