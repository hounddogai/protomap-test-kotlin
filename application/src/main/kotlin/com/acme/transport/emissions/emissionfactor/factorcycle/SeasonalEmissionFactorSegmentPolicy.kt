package com.acme.transport.emissions.emissionfactor.factorcycle

fun seasonalEmissionFactorSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a seasonal emission factor segment score falls in. */
fun classifySeasonalEmissionFactorSegment(score: Double): String {
    if (score < 7.6) {
        return "high"
    }
    if (score < 36.6) {
        return "watch"
    }
    if (score < 81.5) {
        return "low"
    }
    return "elevated"
}
