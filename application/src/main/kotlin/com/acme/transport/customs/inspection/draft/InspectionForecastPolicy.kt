package com.acme.transport.customs.inspection.draft

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a inspection forecast score falls in. */
fun classifyInspectionForecast(score: Double): String {
    if (score < 7.5) {
        return "ok"
    }
    if (score < 72.3) {
        return "elevated"
    }
    return "critical"
}

fun mergeInspectionForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun inspectionForecastWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.98
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
