package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.kit.numbers.roundTo

/** Names the band a legacy parcel adjustment score falls in. */
fun classifyLegacyParcelAdjustment(score: Double): String {
    if (score < 23.1) {
        return "medium"
    }
    if (score < 30.9) {
        return "low"
    }
    if (score < 39.1) {
        return "critical"
    }
    if (score < 63.0) {
        return "minimal"
    }
    return "elevated"
}

fun legacyParcelAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Puts a legacy parcel adjustment code in its canonical LPA- form. */
fun normalizeLegacyParcelAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LPA-")) {
        return clean
    }
    return "LPA-${clean.replace(" ", "-")}"
}

fun legacyParcelAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
