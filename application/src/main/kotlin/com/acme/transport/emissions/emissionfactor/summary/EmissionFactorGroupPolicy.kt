package com.acme.transport.emissions.emissionfactor.summary

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a emission factor group code in its canonical EFG- form. */
fun normalizeEmissionFactorGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("EFG-")) {
        return clean
    }
    return "EFG-${clean.replace(" ", "-")}"
}

fun emissionFactorGroupBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun emissionFactorGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

fun emissionFactorGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.13
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
