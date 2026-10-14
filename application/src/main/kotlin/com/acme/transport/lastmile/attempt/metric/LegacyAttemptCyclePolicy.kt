package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.numbers.roundTo

fun legacyAttemptCycleBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a legacy attempt cycle code in its canonical LAC- form. */
fun normalizeLegacyAttemptCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LAC-")) {
        return clean
    }
    return "LAC-${clean.replace(" ", "-")}"
}

/** Names the band a legacy attempt cycle score falls in. */
fun classifyLegacyAttemptCycle(score: Double): String {
    if (score < 52.5) {
        return "minimal"
    }
    if (score < 72.5) {
        return "ok"
    }
    return "high"
}
