package com.acme.transport.safety.scorecard.cycle

import com.acme.transport.kit.numbers.roundTo

fun legacyScorecardCycleBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a legacy scorecard cycle code in its canonical LSC- form. */
fun normalizeLegacyScorecardCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LSC-")) {
        return clean
    }
    return "LSC-${clean.replace(" ", "-")}"
}

/** Names the band a legacy scorecard cycle score falls in. */
fun classifyLegacyScorecardCycle(score: Double): String {
    if (score < 57.5) {
        return "high"
    }
    if (score < 59.3) {
        return "minimal"
    }
    return "watch"
}
