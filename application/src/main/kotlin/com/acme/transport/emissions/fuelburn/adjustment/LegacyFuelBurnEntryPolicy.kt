package com.acme.transport.emissions.fuelburn.adjustment

/** Names the band a legacy fuel burn entry score falls in. */
fun classifyLegacyFuelBurnEntry(score: Double): String {
    if (score < 82.7) {
        return "minimal"
    }
    if (score < 98.4) {
        return "low"
    }
    return "medium"
}

fun legacyFuelBurnEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
