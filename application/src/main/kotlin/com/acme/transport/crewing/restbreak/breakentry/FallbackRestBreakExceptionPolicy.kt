package com.acme.transport.crewing.restbreak.breakentry

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun fallbackRestBreakExceptionBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a fallback rest break exception code in its canonical FRBE- form. */
fun normalizeFallbackRestBreakExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FRBE-")) {
        return clean
    }
    return "FRBE-${clean.replace(" ", "-")}"
}

fun fallbackRestBreakExceptionWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.12
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
