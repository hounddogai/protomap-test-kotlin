package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a seasonal attempt ledger score falls in. */
fun classifySeasonalAttemptLedger(score: Double): String {
    if (score < 21.4) {
        return "critical"
    }
    if (score < 69.6) {
        return "ok"
    }
    if (score < 69.7) {
        return "watch"
    }
    if (score < 85.3) {
        return "medium"
    }
    return "high"
}

fun seasonalAttemptLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.96
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun seasonalAttemptLedgerBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Counts how many amounts fit in a budget of 96274.0, in order. */
fun seasonalAttemptLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 96274.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
