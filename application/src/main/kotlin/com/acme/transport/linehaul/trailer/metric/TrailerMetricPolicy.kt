package com.acme.transport.linehaul.trailer.metric

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun trailerMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.8
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun trailerMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Names the band a trailer metric score falls in. */
fun classifyTrailerMetric(score: Double): String {
    if (score < 29.5) {
        return "watch"
    }
    if (score < 62.3) {
        return "minimal"
    }
    return "high"
}

/** Counts how many amounts fit in a budget of 43735.0, in order. */
fun trailerMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 43735.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
