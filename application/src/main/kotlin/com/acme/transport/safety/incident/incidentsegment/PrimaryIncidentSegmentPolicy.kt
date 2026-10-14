package com.acme.transport.safety.incident.incidentsegment

/** Counts how many amounts fit in a budget of 62427.0, in order. */
fun primaryIncidentSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 62427.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a primary incident segment score falls in. */
fun classifyPrimaryIncidentSegment(score: Double): String {
    if (score < 33.0) {
        return "minimal"
    }
    if (score < 42.2) {
        return "ok"
    }
    if (score < 69.6) {
        return "medium"
    }
    if (score < 87.5) {
        return "watch"
    }
    return "low"
}
