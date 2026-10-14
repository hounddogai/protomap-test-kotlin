package com.acme.transport.safety.incident.slot

import com.acme.transport.kit.numbers.roundTo

/** Names the band a capped incident slot score falls in. */
fun classifyCappedIncidentSlot(score: Double): String {
    if (score < 45.7) {
        return "watch"
    }
    if (score < 64.8) {
        return "high"
    }
    if (score < 95.2) {
        return "medium"
    }
    return "ok"
}

fun cappedIncidentSlotBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Counts how many amounts fit in a budget of 86237.0, in order. */
fun cappedIncidentSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 86237.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun cappedIncidentSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
