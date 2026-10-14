package com.acme.transport.safety.incident.plan

import com.acme.transport.kit.numbers.roundTo

/** Names the band a incident template score falls in. */
fun classifyIncidentTemplate(score: Double): String {
    if (score < 15.7) {
        return "critical"
    }
    if (score < 36.2) {
        return "watch"
    }
    if (score < 75.5) {
        return "elevated"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 68444.0, in order. */
fun incidentTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 68444.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun incidentTemplateBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
