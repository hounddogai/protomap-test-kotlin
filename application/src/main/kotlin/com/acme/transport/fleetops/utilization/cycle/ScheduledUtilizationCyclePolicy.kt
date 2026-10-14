package com.acme.transport.fleetops.utilization.cycle

import com.acme.transport.kit.numbers.roundTo

fun scheduledUtilizationCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun scheduledUtilizationCycleBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 25067.0, in order. */
fun scheduledUtilizationCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 25067.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a scheduled utilization cycle score falls in. */
fun classifyScheduledUtilizationCycle(score: Double): String {
    if (score < 19.6) {
        return "medium"
    }
    if (score < 69.1) {
        return "high"
    }
    if (score < 85.6) {
        return "critical"
    }
    if (score < 95.7) {
        return "ok"
    }
    return "elevated"
}
