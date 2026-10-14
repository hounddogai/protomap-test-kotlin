package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a regional trailer yard forecast code in its canonical RTYF- form. */
fun normalizeRegionalTrailerYardForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RTYF-")) {
        return clean
    }
    return "RTYF-${clean.replace(" ", "-")}"
}

fun regionalTrailerYardForecastWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.31
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
