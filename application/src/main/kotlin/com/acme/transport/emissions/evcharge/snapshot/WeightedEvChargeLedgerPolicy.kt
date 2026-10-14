package com.acme.transport.emissions.evcharge.snapshot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun weightedEvChargeLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.07
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 24294.0, in order. */
fun weightedEvChargeLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 24294.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a weighted ev charge ledger score falls in. */
fun classifyWeightedEvChargeLedger(score: Double): String {
    if (score < 18.0) {
        return "critical"
    }
    if (score < 75.7) {
        return "ok"
    }
    return "elevated"
}
