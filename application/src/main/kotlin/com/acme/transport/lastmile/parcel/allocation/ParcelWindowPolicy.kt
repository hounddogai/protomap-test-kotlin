package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a parcel window code in its canonical PW- form. */
fun normalizeParcelWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PW-")) {
        return clean
    }
    return "PW-${clean.replace(" ", "-")}"
}

fun parcelWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.59
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun parcelWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
