package com.acme.transport.linehaul.capacity.entry

import com.acme.transport.kit.numbers.roundTo

fun globalCapacityReservationBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a global capacity reservation code in its canonical GCR- form. */
fun normalizeGlobalCapacityReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GCR-")) {
        return clean
    }
    return "GCR-${clean.replace(" ", "-")}"
}
