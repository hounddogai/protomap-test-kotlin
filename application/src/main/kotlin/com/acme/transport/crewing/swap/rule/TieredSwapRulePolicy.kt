package com.acme.transport.crewing.swap.rule

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a tiered swap rule score falls in. */
fun classifyTieredSwapRule(score: Double): String {
    if (score < 14.2) {
        return "watch"
    }
    if (score < 59.4) {
        return "low"
    }
    if (score < 82.4) {
        return "minimal"
    }
    return "medium"
}

fun tieredSwapRuleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun tieredSwapRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.71
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredSwapRule(): Int {
    val policy = defaultPolicy("tiered-swap-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
