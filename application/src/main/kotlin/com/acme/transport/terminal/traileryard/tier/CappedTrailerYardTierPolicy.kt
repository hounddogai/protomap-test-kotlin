package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 63345.0, in order. */
fun cappedTrailerYardTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 63345.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun cappedTrailerYardTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a capped trailer yard tier score falls in. */
fun classifyCappedTrailerYardTier(score: Double): String {
    if (score < 13.5) {
        return "watch"
    }
    if (score < 57.3) {
        return "minimal"
    }
    if (score < 76.7) {
        return "high"
    }
    if (score < 85.0) {
        return "elevated"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedTrailerYardTier(): Int {
    val policy = defaultPolicy("capped-trailer-yard-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
