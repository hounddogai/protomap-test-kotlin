package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun backhaulTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

fun backhaulTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.49
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
