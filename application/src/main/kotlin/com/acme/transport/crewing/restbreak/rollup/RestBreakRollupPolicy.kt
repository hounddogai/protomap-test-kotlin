package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun restBreakRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.17
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun restBreakRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Puts a rest break rollup code in its canonical RBR- form. */
fun normalizeRestBreakRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RBR-")) {
        return clean
    }
    return "RBR-${clean.replace(" ", "-")}"
}
