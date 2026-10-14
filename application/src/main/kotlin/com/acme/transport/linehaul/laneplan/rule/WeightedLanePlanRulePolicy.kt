package com.acme.transport.linehaul.laneplan.rule

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun weightedLanePlanRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Names the band a weighted lane plan rule score falls in. */
fun classifyWeightedLanePlanRule(score: Double): String {
    if (score < 33.4) {
        return "low"
    }
    if (score < 55.9) {
        return "watch"
    }
    if (score < 78.3) {
        return "minimal"
    }
    return "medium"
}

fun weightedLanePlanRuleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun weightedLanePlanRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.1
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
