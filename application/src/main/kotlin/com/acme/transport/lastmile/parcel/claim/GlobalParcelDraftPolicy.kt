package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun globalParcelDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun globalParcelDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.78
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
