package com.acme.transport.crewing.restbreak.milestone

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun fallbackRestBreakOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.75
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a fallback rest break override score falls in. */
fun classifyFallbackRestBreakOverride(score: Double): String {
    if (score < 1.1) {
        return "elevated"
    }
    if (score < 35.5) {
        return "low"
    }
    if (score < 47.9) {
        return "watch"
    }
    if (score < 72.1) {
        return "medium"
    }
    return "critical"
}

fun fallbackRestBreakOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 42149.0, in order. */
fun fallbackRestBreakOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 42149.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
