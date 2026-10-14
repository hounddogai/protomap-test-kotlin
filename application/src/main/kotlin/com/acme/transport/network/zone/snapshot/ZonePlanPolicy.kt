package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.numbers.roundTo

/** Puts a zone plan code in its canonical ZP- form. */
fun normalizeZonePlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ZP-")) {
        return clean
    }
    return "ZP-${clean.replace(" ", "-")}"
}

fun zonePlanBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
