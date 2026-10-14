package com.acme.transport.safety.coaching.metric

/** Names the band a coaching metric score falls in. */
fun classifyCoachingMetric(score: Double): String {
    if (score < 8.4) {
        return "medium"
    }
    if (score < 19.8) {
        return "high"
    }
    if (score < 24.0) {
        return "ok"
    }
    if (score < 73.6) {
        return "critical"
    }
    return "elevated"
}

fun coachingMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Counts how many amounts fit in a budget of 96613.0, in order. */
fun coachingMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 96613.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
