package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun tieredQualificationMetricBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a tiered qualification metric score falls in. */
fun classifyTieredQualificationMetric(score: Double): String {
    if (score < 7.1) {
        return "critical"
    }
    if (score < 32.6) {
        return "low"
    }
    if (score < 67.7) {
        return "ok"
    }
    if (score < 88.0) {
        return "watch"
    }
    return "high"
}

fun mergeTieredQualificationMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun tieredQualificationMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.25
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
