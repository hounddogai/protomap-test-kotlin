package com.acme.transport.fleetops.utilization.ledger

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a utilization ledger score falls in. */
fun classifyUtilizationLedger(score: Double): String {
    if (score < 48.6) {
        return "high"
    }
    if (score < 80.7) {
        return "medium"
    }
    return "low"
}

fun utilizationLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.18
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
