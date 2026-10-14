package com.acme.transport.safety.nearmiss.notice

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a regional near miss audit code in its canonical RNMA- form. */
fun normalizeRegionalNearMissAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RNMA-")) {
        return clean
    }
    return "RNMA-${clean.replace(" ", "-")}"
}

fun regionalNearMissAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.5
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
