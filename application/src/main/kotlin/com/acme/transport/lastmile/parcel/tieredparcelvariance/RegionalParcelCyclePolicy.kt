package com.acme.transport.lastmile.parcel.tieredparcelvariance

import com.acme.transport.kit.numbers.roundTo

/** Puts a regional parcel cycle code in its canonical RPC- form. */
fun normalizeRegionalParcelCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RPC-")) {
        return clean
    }
    return "RPC-${clean.replace(" ", "-")}"
}

fun regionalParcelCycleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
