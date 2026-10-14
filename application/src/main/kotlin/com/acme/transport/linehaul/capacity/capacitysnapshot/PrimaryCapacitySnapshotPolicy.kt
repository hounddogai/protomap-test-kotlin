package com.acme.transport.linehaul.capacity.capacitysnapshot

/** Counts how many amounts fit in a budget of 71176.0, in order. */
fun primaryCapacitySnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71176.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a primary capacity snapshot score falls in. */
fun classifyPrimaryCapacitySnapshot(score: Double): String {
    if (score < 39.6) {
        return "ok"
    }
    if (score < 63.3) {
        return "high"
    }
    return "minimal"
}
