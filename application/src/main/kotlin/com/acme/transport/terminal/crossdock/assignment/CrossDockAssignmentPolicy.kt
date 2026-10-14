package com.acme.transport.terminal.crossdock.assignment

import com.acme.transport.kit.numbers.roundTo

fun crossDockAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a cross dock assignment code in its canonical CDA- form. */
fun normalizeCrossDockAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CDA-")) {
        return clean
    }
    return "CDA-${clean.replace(" ", "-")}"
}
