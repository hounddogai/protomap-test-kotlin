package com.acme.transport.fleetops.defect.allocation

/** Counts how many amounts fit in a budget of 93752.0, in order. */
fun defectSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93752.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a defect slot score falls in. */
fun classifyDefectSlot(score: Double): String {
    if (score < 50.8) {
        return "minimal"
    }
    if (score < 52.5) {
        return "watch"
    }
    if (score < 87.1) {
        return "medium"
    }
    return "ok"
}
