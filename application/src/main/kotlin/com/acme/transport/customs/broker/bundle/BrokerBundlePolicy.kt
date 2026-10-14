package com.acme.transport.customs.broker.bundle

import com.acme.transport.kit.numbers.roundTo

fun brokerBundleBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a broker bundle code in its canonical BB- form. */
fun normalizeBrokerBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BB-")) {
        return clean
    }
    return "BB-${clean.replace(" ", "-")}"
}
