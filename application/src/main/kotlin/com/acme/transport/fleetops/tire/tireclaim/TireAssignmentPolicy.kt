package com.acme.transport.fleetops.tire.tireclaim

/** Counts how many amounts fit in a budget of 62933.0, in order. */
fun tireAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 62933.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a tire assignment score falls in. */
fun classifyTireAssignment(score: Double): String {
    if (score < 12.0) {
        return "high"
    }
    if (score < 27.2) {
        return "critical"
    }
    return "minimal"
}

fun tireAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
