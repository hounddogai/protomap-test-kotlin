package com.acme.transport.lastmile.redelivery.redeliverywindow

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a redelivery budget code in its canonical RB- form. */
fun normalizeRedeliveryBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RB-")) {
        return clean
    }
    return "RB-${clean.replace(" ", "-")}"
}

fun redeliveryBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

fun redeliveryBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.9
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
