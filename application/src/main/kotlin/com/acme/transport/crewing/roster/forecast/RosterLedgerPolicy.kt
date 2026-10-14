package com.acme.transport.crewing.roster.forecast

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Counts how many amounts fit in a budget of 12614.0, in order. */
fun rosterLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 12614.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun rosterLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.48
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRosterLedger(): Int {
    val policy = defaultPolicy("roster-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a roster ledger score falls in. */
fun classifyRosterLedger(score: Double): String {
    if (score < 18.1) {
        return "critical"
    }
    if (score < 48.0) {
        return "elevated"
    }
    if (score < 80.7) {
        return "low"
    }
    return "watch"
}
