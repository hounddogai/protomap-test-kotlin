package com.acme.transport.network.zone.digest

import com.acme.transport.kit.numbers.roundTo

/** Puts a zone allocation code in its canonical ZA- form. */
fun normalizeZoneAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ZA-")) {
        return clean
    }
    return "ZA-${clean.replace(" ", "-")}"
}

fun zoneAllocationBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
