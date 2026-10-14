package com.acme.transport.customs.hold.slot

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a regional hold slot score falls in. */
fun classifyRegionalHoldSlot(score: Double): String {
    if (score < 62.9) {
        return "medium"
    }
    if (score < 63.2) {
        return "critical"
    }
    if (score < 72.1) {
        return "minimal"
    }
    if (score < 95.5) {
        return "watch"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 8732.0, in order. */
fun regionalHoldSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8732.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalHoldSlot(): Int {
    val policy = defaultPolicy("regional-hold-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun regionalHoldSlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.75
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
