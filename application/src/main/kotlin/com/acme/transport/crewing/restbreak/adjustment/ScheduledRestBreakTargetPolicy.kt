package com.acme.transport.crewing.restbreak.adjustment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a scheduled rest break target code in its canonical SRBT- form. */
fun normalizeScheduledRestBreakTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRBT-")) {
        return clean
    }
    return "SRBT-${clean.replace(" ", "-")}"
}

fun scheduledRestBreakTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.51
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
