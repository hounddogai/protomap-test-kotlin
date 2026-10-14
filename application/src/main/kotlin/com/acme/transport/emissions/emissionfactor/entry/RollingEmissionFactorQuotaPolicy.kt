package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun rollingEmissionFactorQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.6
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun rollingEmissionFactorQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
