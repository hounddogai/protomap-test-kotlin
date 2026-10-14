package com.acme.transport.freightpay.dispute.adaptivedisputeforecast

import com.acme.transport.kit.numbers.roundTo

/** Puts a strict dispute forecast code in its canonical SDF- form. */
fun normalizeStrictDisputeForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDF-")) {
        return clean
    }
    return "SDF-${clean.replace(" ", "-")}"
}

fun strictDisputeForecastBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
