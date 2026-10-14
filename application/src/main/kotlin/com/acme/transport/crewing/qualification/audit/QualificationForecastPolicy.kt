package com.acme.transport.crewing.qualification.audit

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun qualificationForecastWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.26
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a qualification forecast code in its canonical QF- form. */
fun normalizeQualificationForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("QF-")) {
        return clean
    }
    return "QF-${clean.replace(" ", "-")}"
}
