package com.acme.transport.network.cutoff.ledger

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 38891.0, in order. */
fun manualCutoffLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 38891.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a manual cutoff ledger score falls in. */
fun classifyManualCutoffLedger(score: Double): String {
    if (score < 2.3) {
        return "critical"
    }
    if (score < 44.8) {
        return "minimal"
    }
    if (score < 56.4) {
        return "high"
    }
    if (score < 80.1) {
        return "elevated"
    }
    return "ok"
}

fun manualCutoffLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.12
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
