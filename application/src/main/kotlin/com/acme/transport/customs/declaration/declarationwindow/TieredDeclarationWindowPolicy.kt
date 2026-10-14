package com.acme.transport.customs.declaration.declarationwindow

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun tieredDeclarationWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a tiered declaration window score falls in. */
fun classifyTieredDeclarationWindow(score: Double): String {
    if (score < 19.8) {
        return "medium"
    }
    if (score < 26.3) {
        return "watch"
    }
    return "critical"
}

fun tieredDeclarationWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun tieredDeclarationWindowBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
