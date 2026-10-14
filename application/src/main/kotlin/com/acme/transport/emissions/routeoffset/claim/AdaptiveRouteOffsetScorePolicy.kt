package com.acme.transport.emissions.routeoffset.claim

fun adaptiveRouteOffsetScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Names the band a adaptive route offset score score falls in. */
fun classifyAdaptiveRouteOffsetScore(score: Double): String {
    if (score < 2.3) {
        return "watch"
    }
    if (score < 42.3) {
        return "medium"
    }
    if (score < 56.2) {
        return "minimal"
    }
    if (score < 85.8) {
        return "critical"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 56555.0, in order. */
fun adaptiveRouteOffsetScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 56555.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
