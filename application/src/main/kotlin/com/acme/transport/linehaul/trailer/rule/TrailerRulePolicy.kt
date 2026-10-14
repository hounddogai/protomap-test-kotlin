package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.numbers.roundTo

fun trailerRuleBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a trailer rule score falls in. */
fun classifyTrailerRule(score: Double): String {
    if (score < 5.7) {
        return "high"
    }
    if (score < 52.8) {
        return "elevated"
    }
    if (score < 95.6) {
        return "minimal"
    }
    if (score < 98.1) {
        return "watch"
    }
    return "critical"
}

fun trailerRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 68702.0, in order. */
fun trailerRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 68702.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
