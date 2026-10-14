package com.acme.transport.crewing.availability.threshold

import com.acme.transport.kit.numbers.roundTo

/** Names the band a availability threshold score falls in. */
fun classifyAvailabilityThreshold(score: Double): String {
    if (score < 42.1) {
        return "watch"
    }
    if (score < 93.4) {
        return "elevated"
    }
    return "minimal"
}

/** Puts a availability threshold code in its canonical AT- form. */
fun normalizeAvailabilityThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AT-")) {
        return clean
    }
    return "AT-${clean.replace(" ", "-")}"
}

fun availabilityThresholdBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
