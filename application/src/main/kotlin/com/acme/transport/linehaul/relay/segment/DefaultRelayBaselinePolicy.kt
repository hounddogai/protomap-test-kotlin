package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.numbers.roundTo

/** Puts a default relay baseline code in its canonical DRB- form. */
fun normalizeDefaultRelayBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DRB-")) {
        return clean
    }
    return "DRB-${clean.replace(" ", "-")}"
}

fun defaultRelayBaselineBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
