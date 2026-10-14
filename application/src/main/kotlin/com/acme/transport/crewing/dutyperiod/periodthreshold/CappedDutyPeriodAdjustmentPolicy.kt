package com.acme.transport.crewing.dutyperiod.periodthreshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeCappedDutyPeriodAdjustmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun cappedDutyPeriodAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a capped duty period adjustment score falls in. */
fun classifyCappedDutyPeriodAdjustment(score: Double): String {
    if (score < 26.1) {
        return "high"
    }
    if (score < 49.7) {
        return "elevated"
    }
    return "critical"
}
