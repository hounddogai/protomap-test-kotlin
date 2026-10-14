package com.acme.transport.fleetops.utilization.utilizationledger

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun primaryUtilizationLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.94
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a primary utilization ledger score falls in. */
fun classifyPrimaryUtilizationLedger(score: Double): String {
    if (score < 8.3) {
        return "medium"
    }
    if (score < 72.6) {
        return "ok"
    }
    if (score < 90.9) {
        return "critical"
    }
    return "watch"
}

fun primaryUtilizationLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
