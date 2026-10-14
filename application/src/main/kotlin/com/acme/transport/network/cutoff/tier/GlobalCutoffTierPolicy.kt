package com.acme.transport.network.cutoff.tier

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun globalCutoffTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun globalCutoffTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.48
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
