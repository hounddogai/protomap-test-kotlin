package com.acme.transport.network.node.schedule

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun nodeScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun nodeScheduleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.74
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a node schedule code in its canonical NS- form. */
fun normalizeNodeScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("NS-")) {
        return clean
    }
    return "NS-${clean.replace(" ", "-")}"
}
