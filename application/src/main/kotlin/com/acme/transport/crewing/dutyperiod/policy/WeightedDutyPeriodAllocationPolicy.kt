package com.acme.transport.crewing.dutyperiod.policy

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun weightedDutyPeriodAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.75
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a weighted duty period allocation score falls in. */
fun classifyWeightedDutyPeriodAllocation(score: Double): String {
    if (score < 33.3) {
        return "critical"
    }
    if (score < 56.2) {
        return "medium"
    }
    if (score < 87.6) {
        return "low"
    }
    return "elevated"
}

/** Puts a weighted duty period allocation code in its canonical WDPA- form. */
fun normalizeWeightedDutyPeriodAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WDPA-")) {
        return clean
    }
    return "WDPA-${clean.replace(" ", "-")}"
}
