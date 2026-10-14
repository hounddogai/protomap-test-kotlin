package com.acme.transport.emissions.carbonreport.reservation

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun fallbackCarbonReportVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.85
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a fallback carbon report variance code in its canonical FCRV- form. */
fun normalizeFallbackCarbonReportVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FCRV-")) {
        return clean
    }
    return "FCRV-${clean.replace(" ", "-")}"
}
