package com.acme.transport.customs.inspection.estimate

import com.acme.transport.kit.numbers.roundTo

/** Names the band a lenient inspection estimate score falls in. */
fun classifyLenientInspectionEstimate(score: Double): String {
    if (score < 37.9) {
        return "medium"
    }
    if (score < 52.1) {
        return "low"
    }
    if (score < 54.4) {
        return "ok"
    }
    if (score < 68.4) {
        return "high"
    }
    return "critical"
}

/** Puts a lenient inspection estimate code in its canonical LIE- form. */
fun normalizeLenientInspectionEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LIE-")) {
        return clean
    }
    return "LIE-${clean.replace(" ", "-")}"
}

fun lenientInspectionEstimateBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
