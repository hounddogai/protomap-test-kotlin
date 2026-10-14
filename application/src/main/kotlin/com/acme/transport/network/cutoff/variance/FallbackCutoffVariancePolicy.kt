package com.acme.transport.network.cutoff.variance

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackCutoffVariance(): Int {
    val policy = defaultPolicy("fallback-cutoff-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun fallbackCutoffVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 25423.0, in order. */
fun fallbackCutoffVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 25423.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fallbackCutoffVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.42
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
