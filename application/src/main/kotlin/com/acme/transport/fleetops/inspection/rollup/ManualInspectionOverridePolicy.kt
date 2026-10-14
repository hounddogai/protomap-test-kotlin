package com.acme.transport.fleetops.inspection.rollup

import com.acme.transport.kit.numbers.roundTo

/** Names the band a manual inspection override score falls in. */
fun classifyManualInspectionOverride(score: Double): String {
    if (score < 40.8) {
        return "ok"
    }
    if (score < 48.8) {
        return "minimal"
    }
    if (score < 72.9) {
        return "elevated"
    }
    return "watch"
}

/** Puts a manual inspection override code in its canonical MIO- form. */
fun normalizeManualInspectionOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MIO-")) {
        return clean
    }
    return "MIO-${clean.replace(" ", "-")}"
}

fun manualInspectionOverrideBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
