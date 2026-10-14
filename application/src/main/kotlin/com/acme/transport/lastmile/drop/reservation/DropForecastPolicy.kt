package com.acme.transport.lastmile.drop.reservation

import com.acme.transport.kit.numbers.roundTo

fun dropForecastBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a drop forecast code in its canonical DF- form. */
fun normalizeDropForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DF-")) {
        return clean
    }
    return "DF-${clean.replace(" ", "-")}"
}
