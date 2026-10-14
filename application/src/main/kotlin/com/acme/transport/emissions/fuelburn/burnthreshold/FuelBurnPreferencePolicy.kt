package com.acme.transport.emissions.fuelburn.burnthreshold

/** Names the band a fuel burn preference score falls in. */
fun classifyFuelBurnPreference(score: Double): String {
    if (score < 10.1) {
        return "watch"
    }
    if (score < 20.5) {
        return "critical"
    }
    if (score < 65.9) {
        return "medium"
    }
    return "low"
}

fun fuelBurnPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
