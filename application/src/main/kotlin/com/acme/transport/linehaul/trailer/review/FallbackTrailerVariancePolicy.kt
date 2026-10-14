package com.acme.transport.linehaul.trailer.review

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun fallbackTrailerVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.65
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a fallback trailer variance code in its canonical FTV- form. */
fun normalizeFallbackTrailerVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FTV-")) {
        return clean
    }
    return "FTV-${clean.replace(" ", "-")}"
}
