package com.acme.transport.terminal.dwell.exception

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun globalDwellExceptionWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.75
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a global dwell exception code in its canonical GDE- form. */
fun normalizeGlobalDwellExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GDE-")) {
        return clean
    }
    return "GDE-${clean.replace(" ", "-")}"
}

fun globalDwellExceptionBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
