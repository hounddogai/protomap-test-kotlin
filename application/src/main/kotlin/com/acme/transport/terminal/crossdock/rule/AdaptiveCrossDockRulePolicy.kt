package com.acme.transport.terminal.crossdock.rule

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun adaptiveCrossDockRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun adaptiveCrossDockRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.94
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a adaptive cross dock rule code in its canonical ACDR- form. */
fun normalizeAdaptiveCrossDockRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ACDR-")) {
        return clean
    }
    return "ACDR-${clean.replace(" ", "-")}"
}
