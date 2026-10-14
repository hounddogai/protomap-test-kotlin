package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeAdaptiveDutyPeriodSegmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun adaptiveDutyPeriodSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.29
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun adaptiveDutyPeriodSegmentBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a adaptive duty period segment score falls in. */
fun classifyAdaptiveDutyPeriodSegment(score: Double): String {
    if (score < 3.5) {
        return "elevated"
    }
    if (score < 8.0) {
        return "critical"
    }
    if (score < 74.4) {
        return "low"
    }
    if (score < 96.6) {
        return "watch"
    }
    return "medium"
}
