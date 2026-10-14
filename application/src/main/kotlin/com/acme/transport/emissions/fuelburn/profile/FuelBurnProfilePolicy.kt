package com.acme.transport.emissions.fuelburn.profile

/** Names the band a fuel burn profile score falls in. */
fun classifyFuelBurnProfile(score: Double): String {
    if (score < 14.5) {
        return "watch"
    }
    if (score < 69.2) {
        return "medium"
    }
    return "minimal"
}

fun fuelBurnProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
