package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.numbers.roundTo

/** Names the band a lenient parcel rollup score falls in. */
fun classifyLenientParcelRollup(score: Double): String {
    if (score < 26.6) {
        return "elevated"
    }
    if (score < 57.5) {
        return "medium"
    }
    if (score < 60.3) {
        return "critical"
    }
    if (score < 94.2) {
        return "high"
    }
    return "minimal"
}

fun lenientParcelRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 45125.0, in order. */
fun lenientParcelRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 45125.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun lenientParcelRollupBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
