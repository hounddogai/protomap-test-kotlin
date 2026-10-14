package com.acme.transport.customs.inspection.inspectionestimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun inspectionTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

fun mergeInspectionTierLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun inspectionTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.26
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a inspection tier score falls in. */
fun classifyInspectionTier(score: Double): String {
    if (score < 5.0) {
        return "medium"
    }
    if (score < 19.2) {
        return "minimal"
    }
    if (score < 68.2) {
        return "watch"
    }
    if (score < 81.5) {
        return "ok"
    }
    return "low"
}
