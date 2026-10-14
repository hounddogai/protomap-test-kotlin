package com.acme.transport.customs.declaration.summary

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun declarationWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.54
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun declarationWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
