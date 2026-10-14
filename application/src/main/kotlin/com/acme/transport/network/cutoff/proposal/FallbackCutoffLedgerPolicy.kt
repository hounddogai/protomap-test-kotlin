package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a fallback cutoff ledger score falls in. */
fun classifyFallbackCutoffLedger(score: Double): String {
    if (score < 2.2) {
        return "medium"
    }
    if (score < 40.8) {
        return "critical"
    }
    if (score < 64.4) {
        return "high"
    }
    return "ok"
}

/** Puts a fallback cutoff ledger code in its canonical FCL- form. */
fun normalizeFallbackCutoffLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FCL-")) {
        return clean
    }
    return "FCL-${clean.replace(" ", "-")}"
}

fun fallbackCutoffLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.31
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
