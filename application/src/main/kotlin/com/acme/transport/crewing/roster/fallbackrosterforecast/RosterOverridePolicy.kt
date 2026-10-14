package com.acme.transport.crewing.roster.fallbackrosterforecast

import com.acme.transport.kit.numbers.roundTo

fun rosterOverrideBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a roster override score falls in. */
fun classifyRosterOverride(score: Double): String {
    if (score < 7.2) {
        return "medium"
    }
    if (score < 51.5) {
        return "low"
    }
    if (score < 84.1) {
        return "minimal"
    }
    if (score < 87.2) {
        return "watch"
    }
    return "elevated"
}
