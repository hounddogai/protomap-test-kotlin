package com.acme.transport.terminal.traileryard.score

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a weighted trailer yard tier score falls in. */
fun classifyWeightedTrailerYardTier(score: Double): String {
    if (score < 18.7) {
        return "minimal"
    }
    if (score < 41.3) {
        return "watch"
    }
    if (score < 43.5) {
        return "high"
    }
    if (score < 72.7) {
        return "medium"
    }
    return "ok"
}

fun weightedTrailerYardTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.18
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun weightedTrailerYardTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedTrailerYardTier(): Int {
    val policy = defaultPolicy("weighted-trailer-yard-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
