package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.kit.numbers.roundTo

fun defaultCrossDockScheduleBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a default cross dock schedule code in its canonical DCDS- form. */
fun normalizeDefaultCrossDockScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DCDS-")) {
        return clean
    }
    return "DCDS-${clean.replace(" ", "-")}"
}
