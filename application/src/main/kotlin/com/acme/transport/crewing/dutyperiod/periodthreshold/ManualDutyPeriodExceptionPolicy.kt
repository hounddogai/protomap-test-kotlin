package com.acme.transport.crewing.dutyperiod.periodthreshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun manualDutyPeriodExceptionWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.64
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun manualDutyPeriodExceptionSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a manual duty period exception score falls in. */
fun classifyManualDutyPeriodException(score: Double): String {
    if (score < 65.6) {
        return "low"
    }
    if (score < 66.1) {
        return "high"
    }
    if (score < 71.6) {
        return "critical"
    }
    if (score < 90.2) {
        return "elevated"
    }
    return "ok"
}

fun mergeManualDutyPeriodExceptionLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
