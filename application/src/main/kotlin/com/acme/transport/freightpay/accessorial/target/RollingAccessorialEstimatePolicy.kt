package com.acme.transport.freightpay.accessorial.target

import com.acme.transport.kit.numbers.roundTo

fun rollingAccessorialEstimateBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a rolling accessorial estimate code in its canonical RAE- form. */
fun normalizeRollingAccessorialEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RAE-")) {
        return clean
    }
    return "RAE-${clean.replace(" ", "-")}"
}
