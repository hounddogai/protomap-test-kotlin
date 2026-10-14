package com.acme.transport.fleetops.tire.plan

/** Counts how many amounts fit in a budget of 37511.0, in order. */
fun pendingTireScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37511.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun pendingTireScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Names the band a pending tire schedule score falls in. */
fun classifyPendingTireSchedule(score: Double): String {
    if (score < 12.9) {
        return "critical"
    }
    if (score < 19.8) {
        return "medium"
    }
    if (score < 24.8) {
        return "low"
    }
    if (score < 61.5) {
        return "ok"
    }
    return "minimal"
}
