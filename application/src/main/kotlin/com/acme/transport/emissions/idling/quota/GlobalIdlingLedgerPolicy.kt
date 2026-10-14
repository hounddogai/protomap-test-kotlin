package com.acme.transport.emissions.idling.quota

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun globalIdlingLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.93
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 50629.0, in order. */
fun globalIdlingLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 50629.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalIdlingLedger(): Int {
    val policy = defaultPolicy("global-idling-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a global idling ledger score falls in. */
fun classifyGlobalIdlingLedger(score: Double): String {
    if (score < 12.3) {
        return "medium"
    }
    if (score < 55.6) {
        return "low"
    }
    if (score < 97.8) {
        return "critical"
    }
    return "minimal"
}
