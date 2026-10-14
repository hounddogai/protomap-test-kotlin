package com.acme.transport.lastmile.drop.quota

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun primaryDropProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.45
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun primaryDropProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Puts a primary drop profile code in its canonical PDP- form. */
fun normalizePrimaryDropProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PDP-")) {
        return clean
    }
    return "PDP-${clean.replace(" ", "-")}"
}

fun primaryDropProfileBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
