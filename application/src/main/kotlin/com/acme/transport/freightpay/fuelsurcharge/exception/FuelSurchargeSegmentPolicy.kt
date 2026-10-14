package com.acme.transport.freightpay.fuelsurcharge.exception

import com.acme.transport.kit.numbers.roundTo

/** Names the band a fuel surcharge segment score falls in. */
fun classifyFuelSurchargeSegment(score: Double): String {
    if (score < 6.5) {
        return "minimal"
    }
    if (score < 29.8) {
        return "critical"
    }
    if (score < 67.9) {
        return "ok"
    }
    return "high"
}

fun fuelSurchargeSegmentBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a fuel surcharge segment code in its canonical FSS- form. */
fun normalizeFuelSurchargeSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FSS-")) {
        return clean
    }
    return "FSS-${clean.replace(" ", "-")}"
}
