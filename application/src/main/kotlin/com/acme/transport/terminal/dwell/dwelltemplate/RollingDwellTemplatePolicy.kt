package com.acme.transport.terminal.dwell.dwelltemplate

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun rollingDwellTemplateBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun rollingDwellTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.84
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 71165.0, in order. */
fun rollingDwellTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71165.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a rolling dwell template score falls in. */
fun classifyRollingDwellTemplate(score: Double): String {
    if (score < 3.3) {
        return "watch"
    }
    if (score < 51.0) {
        return "minimal"
    }
    if (score < 84.0) {
        return "elevated"
    }
    return "critical"
}
