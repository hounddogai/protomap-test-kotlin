package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.kit.numbers.roundTo

fun availabilityRollupBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a availability rollup code in its canonical AR- form. */
fun normalizeAvailabilityRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AR-")) {
        return clean
    }
    return "AR-${clean.replace(" ", "-")}"
}

/** Names the band a availability rollup score falls in. */
fun classifyAvailabilityRollup(score: Double): String {
    if (score < 7.4) {
        return "medium"
    }
    if (score < 64.1) {
        return "critical"
    }
    if (score < 89.9) {
        return "ok"
    }
    if (score < 96.9) {
        return "high"
    }
    return "minimal"
}
