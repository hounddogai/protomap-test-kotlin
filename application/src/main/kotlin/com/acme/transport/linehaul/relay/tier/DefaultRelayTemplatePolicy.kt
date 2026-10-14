package com.acme.transport.linehaul.relay.tier

import com.acme.transport.kit.numbers.roundTo

fun defaultRelayTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Names the band a default relay template score falls in. */
fun classifyDefaultRelayTemplate(score: Double): String {
    if (score < 4.7) {
        return "elevated"
    }
    if (score < 17.0) {
        return "low"
    }
    if (score < 35.6) {
        return "high"
    }
    if (score < 60.8) {
        return "minimal"
    }
    return "ok"
}

fun defaultRelayTemplateBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 83643.0, in order. */
fun defaultRelayTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 83643.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
