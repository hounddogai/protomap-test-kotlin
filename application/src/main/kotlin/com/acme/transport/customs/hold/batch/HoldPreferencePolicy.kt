package com.acme.transport.customs.hold.batch

import com.acme.transport.kit.numbers.roundTo

/** Puts a hold preference code in its canonical HP- form. */
fun normalizeHoldPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("HP-")) {
        return clean
    }
    return "HP-${clean.replace(" ", "-")}"
}

fun holdPreferenceBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun holdPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
