package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a weighted duty period target code in its canonical WDPT- form. */
fun normalizeWeightedDutyPeriodTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WDPT-")) {
        return clean
    }
    return "WDPT-${clean.replace(" ", "-")}"
}

fun weightedDutyPeriodTargetBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun weightedDutyPeriodTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.69
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
