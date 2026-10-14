package com.acme.transport.terminal.traileryard.yardbudget

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun trailerYardPreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.17
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 42762.0, in order. */
fun trailerYardPreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 42762.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTrailerYardPreference(): Int {
    val policy = defaultPolicy("trailer-yard-preference")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a trailer yard preference score falls in. */
fun classifyTrailerYardPreference(score: Double): String {
    if (score < 6.2) {
        return "watch"
    }
    if (score < 78.7) {
        return "critical"
    }
    if (score < 91.5) {
        return "minimal"
    }
    return "low"
}
