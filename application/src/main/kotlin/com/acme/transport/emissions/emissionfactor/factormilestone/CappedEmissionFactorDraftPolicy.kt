package com.acme.transport.emissions.emissionfactor.factormilestone

import com.acme.transport.kit.numbers.roundTo

fun cappedEmissionFactorDraftBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun cappedEmissionFactorDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
