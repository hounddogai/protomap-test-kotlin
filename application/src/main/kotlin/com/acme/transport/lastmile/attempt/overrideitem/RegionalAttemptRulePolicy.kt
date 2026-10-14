package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a regional attempt rule score falls in. */
fun classifyRegionalAttemptRule(score: Double): String {
    if (score < 10.7) {
        return "low"
    }
    if (score < 48.3) {
        return "ok"
    }
    if (score < 50.7) {
        return "minimal"
    }
    if (score < 82.2) {
        return "critical"
    }
    return "watch"
}

fun regionalAttemptRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.82
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 70356.0, in order. */
fun regionalAttemptRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 70356.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
