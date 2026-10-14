package com.acme.transport.customs.inspection.inspectiontarget

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 47160.0, in order. */
fun inspectionManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 47160.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun inspectionManifestBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a inspection manifest score falls in. */
fun classifyInspectionManifest(score: Double): String {
    if (score < 60.3) {
        return "critical"
    }
    if (score < 95.4) {
        return "ok"
    }
    if (score < 98.6) {
        return "low"
    }
    return "medium"
}
