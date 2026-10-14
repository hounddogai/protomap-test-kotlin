package com.acme.transport.lastmile.redelivery.proposal

import com.acme.transport.kit.numbers.roundTo

fun cappedRedeliveryWindowBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a capped redelivery window code in its canonical CRW- form. */
fun normalizeCappedRedeliveryWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CRW-")) {
        return clean
    }
    return "CRW-${clean.replace(" ", "-")}"
}
