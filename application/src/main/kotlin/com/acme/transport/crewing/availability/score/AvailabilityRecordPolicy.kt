package com.acme.transport.crewing.availability.score

/** Names the band a availability record score falls in. */
fun classifyAvailabilityRecord(score: Double): String {
    if (score < 11.3) {
        return "high"
    }
    if (score < 28.5) {
        return "critical"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 31781.0, in order. */
fun availabilityRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 31781.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
