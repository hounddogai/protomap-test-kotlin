package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun primaryServicePlanLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Names the band a primary service plan ledger score falls in. */
fun classifyPrimaryServicePlanLedger(score: Double): String {
    if (score < 20.8) {
        return "ok"
    }
    if (score < 39.9) {
        return "elevated"
    }
    return "medium"
}

fun primaryServicePlanLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.28
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
