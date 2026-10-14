package com.acme.transport.terminal.sortation.forecast

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a regional sortation ledger score falls in. */
fun classifyRegionalSortationLedger(score: Double): String {
    if (score < 25.2) {
        return "critical"
    }
    if (score < 28.7) {
        return "medium"
    }
    if (score < 43.9) {
        return "ok"
    }
    if (score < 59.2) {
        return "low"
    }
    return "high"
}

/** Puts a regional sortation ledger code in its canonical RSL- form. */
fun normalizeRegionalSortationLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RSL-")) {
        return clean
    }
    return "RSL-${clean.replace(" ", "-")}"
}

fun regionalSortationLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.65
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
