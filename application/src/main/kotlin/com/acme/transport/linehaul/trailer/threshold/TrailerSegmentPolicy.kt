package com.acme.transport.linehaul.trailer.threshold

fun trailerSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Names the band a trailer segment score falls in. */
fun classifyTrailerSegment(score: Double): String {
    if (score < 3.8) {
        return "high"
    }
    if (score < 17.5) {
        return "minimal"
    }
    if (score < 91.1) {
        return "ok"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 76172.0, in order. */
fun trailerSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 76172.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
