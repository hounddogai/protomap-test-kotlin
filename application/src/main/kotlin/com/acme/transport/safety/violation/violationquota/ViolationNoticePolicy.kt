package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun violationNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.84
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a violation notice code in its canonical VN- form. */
fun normalizeViolationNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("VN-")) {
        return clean
    }
    return "VN-${clean.replace(" ", "-")}"
}
