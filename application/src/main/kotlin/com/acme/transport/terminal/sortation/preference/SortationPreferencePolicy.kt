package com.acme.transport.terminal.sortation.preference

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Counts how many amounts fit in a budget of 54268.0, in order. */
fun sortationPreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 54268.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a sortation preference score falls in. */
fun classifySortationPreference(score: Double): String {
    if (score < 2.5) {
        return "watch"
    }
    if (score < 68.6) {
        return "low"
    }
    if (score < 93.4) {
        return "high"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSortationPreference(): Int {
    val policy = defaultPolicy("sortation-preference")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun sortationPreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.78
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
