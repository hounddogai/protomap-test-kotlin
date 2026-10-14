package com.acme.transport.safety.violation.violationslot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun scheduledViolationTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.73
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a scheduled violation template code in its canonical SVT- form. */
fun normalizeScheduledViolationTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SVT-")) {
        return clean
    }
    return "SVT-${clean.replace(" ", "-")}"
}
