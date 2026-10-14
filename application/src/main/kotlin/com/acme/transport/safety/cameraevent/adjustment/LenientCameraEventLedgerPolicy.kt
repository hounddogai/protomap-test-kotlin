package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientCameraEventLedger(): Int {
    val policy = defaultPolicy("lenient-camera-event-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a lenient camera event ledger score falls in. */
fun classifyLenientCameraEventLedger(score: Double): String {
    if (score < 28.6) {
        return "watch"
    }
    if (score < 33.4) {
        return "critical"
    }
    if (score < 85.8) {
        return "low"
    }
    return "ok"
}

fun lenientCameraEventLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.37
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 76735.0, in order. */
fun lenientCameraEventLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 76735.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
