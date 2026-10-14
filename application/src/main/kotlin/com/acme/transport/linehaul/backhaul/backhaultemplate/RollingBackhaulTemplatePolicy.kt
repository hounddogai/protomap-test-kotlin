package com.acme.transport.linehaul.backhaul.backhaultemplate

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 23972.0, in order. */
fun rollingBackhaulTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 23972.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun rollingBackhaulTemplateBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a rolling backhaul template score falls in. */
fun classifyRollingBackhaulTemplate(score: Double): String {
    if (score < 54.0) {
        return "elevated"
    }
    if (score < 84.7) {
        return "watch"
    }
    if (score < 92.5) {
        return "medium"
    }
    if (score < 99.4) {
        return "ok"
    }
    return "critical"
}

fun rollingBackhaulTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
