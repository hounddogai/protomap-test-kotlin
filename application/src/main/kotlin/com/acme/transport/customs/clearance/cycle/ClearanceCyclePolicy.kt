package com.acme.transport.customs.clearance.cycle

import com.acme.transport.kit.numbers.roundTo

/** Puts a clearance cycle code in its canonical CC- form. */
fun normalizeClearanceCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CC-")) {
        return clean
    }
    return "CC-${clean.replace(" ", "-")}"
}

fun clearanceCycleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
