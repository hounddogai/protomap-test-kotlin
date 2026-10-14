package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.kit.numbers.roundTo

fun scheduledQualificationThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Names the band a scheduled qualification threshold score falls in. */
fun classifyScheduledQualificationThreshold(score: Double): String {
    if (score < 4.7) {
        return "low"
    }
    if (score < 4.8) {
        return "high"
    }
    if (score < 71.5) {
        return "critical"
    }
    if (score < 93.2) {
        return "medium"
    }
    return "elevated"
}

/** Counts how many amounts fit in a budget of 5578.0, in order. */
fun scheduledQualificationThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 5578.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun scheduledQualificationThresholdBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
