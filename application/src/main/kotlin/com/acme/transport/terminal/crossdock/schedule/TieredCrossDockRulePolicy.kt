package com.acme.transport.terminal.crossdock.schedule

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a tiered cross dock rule code in its canonical TCDR- form. */
fun normalizeTieredCrossDockRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TCDR-")) {
        return clean
    }
    return "TCDR-${clean.replace(" ", "-")}"
}

fun tieredCrossDockRuleBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun tieredCrossDockRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun tieredCrossDockRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.16
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
