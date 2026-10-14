package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun dutyPeriodAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.48
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a duty period allocation score falls in. */
fun classifyDutyPeriodAllocation(score: Double): String {
    if (score < 43.2) {
        return "medium"
    }
    if (score < 69.9) {
        return "high"
    }
    return "minimal"
}

fun dutyPeriodAllocationBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
