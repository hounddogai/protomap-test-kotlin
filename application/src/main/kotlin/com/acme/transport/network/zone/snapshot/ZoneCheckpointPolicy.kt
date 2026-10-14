package com.acme.transport.network.zone.snapshot

/** Counts how many amounts fit in a budget of 88873.0, in order. */
fun zoneCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 88873.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a zone checkpoint score falls in. */
fun classifyZoneCheckpoint(score: Double): String {
    if (score < 54.0) {
        return "ok"
    }
    if (score < 83.0) {
        return "elevated"
    }
    if (score < 90.2) {
        return "medium"
    }
    return "minimal"
}
