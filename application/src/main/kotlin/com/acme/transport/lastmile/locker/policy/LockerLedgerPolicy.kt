package com.acme.transport.lastmile.locker.policy

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a locker ledger score falls in. */
fun classifyLockerLedger(score: Double): String {
    if (score < 9.5) {
        return "minimal"
    }
    if (score < 32.8) {
        return "ok"
    }
    if (score < 33.4) {
        return "medium"
    }
    if (score < 71.2) {
        return "low"
    }
    return "critical"
}

fun lockerLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun lockerLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.79
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
