package com.acme.transport.customs.hold.adjustment

import com.acme.transport.kit.numbers.roundTo

/** Puts a hold cycle code in its canonical HC- form. */
fun normalizeHoldCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("HC-")) {
        return clean
    }
    return "HC-${clean.replace(" ", "-")}"
}

fun holdCycleBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
