package com.acme.transport.network.servicearea.score

import com.acme.transport.kit.numbers.roundTo

/** Names the band a strict service area cycle score falls in. */
fun classifyStrictServiceAreaCycle(score: Double): String {
    if (score < 3.0) {
        return "elevated"
    }
    if (score < 51.9) {
        return "minimal"
    }
    if (score < 74.5) {
        return "low"
    }
    return "ok"
}

/** Puts a strict service area cycle code in its canonical SSAC- form. */
fun normalizeStrictServiceAreaCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SSAC-")) {
        return clean
    }
    return "SSAC-${clean.replace(" ", "-")}"
}

fun strictServiceAreaCycleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
