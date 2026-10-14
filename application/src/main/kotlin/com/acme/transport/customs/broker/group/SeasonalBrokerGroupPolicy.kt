package com.acme.transport.customs.broker.group

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun seasonalBrokerGroupBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a seasonal broker group score falls in. */
fun classifySeasonalBrokerGroup(score: Double): String {
    if (score < 42.8) {
        return "high"
    }
    if (score < 56.4) {
        return "minimal"
    }
    if (score < 84.1) {
        return "watch"
    }
    if (score < 87.1) {
        return "low"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 1388.0, in order. */
fun seasonalBrokerGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 1388.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalBrokerGroup(): Int {
    val policy = defaultPolicy("seasonal-broker-group")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
