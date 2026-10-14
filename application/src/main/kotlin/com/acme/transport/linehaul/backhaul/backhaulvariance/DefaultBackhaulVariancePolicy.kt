package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.kit.numbers.roundTo

fun defaultBackhaulVarianceBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a default backhaul variance code in its canonical DBV- form. */
fun normalizeDefaultBackhaulVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DBV-")) {
        return clean
    }
    return "DBV-${clean.replace(" ", "-")}"
}

fun defaultBackhaulVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
