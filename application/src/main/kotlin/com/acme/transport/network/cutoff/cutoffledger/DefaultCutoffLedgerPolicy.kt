package com.acme.transport.network.cutoff.cutoffledger

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun defaultCutoffLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.07
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a default cutoff ledger score falls in. */
fun classifyDefaultCutoffLedger(score: Double): String {
    if (score < 49.2) {
        return "low"
    }
    if (score < 67.7) {
        return "elevated"
    }
    return "watch"
}

fun defaultCutoffLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
