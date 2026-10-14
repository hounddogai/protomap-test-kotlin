package com.acme.transport.network.servicearea.milestone

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun seasonalServiceAreaLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.26
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a seasonal service area ledger score falls in. */
fun classifySeasonalServiceAreaLedger(score: Double): String {
    if (score < 11.0) {
        return "elevated"
    }
    if (score < 20.4) {
        return "low"
    }
    if (score < 52.5) {
        return "ok"
    }
    if (score < 75.3) {
        return "watch"
    }
    return "critical"
}
