package com.acme.transport.customs.inspection.overrideitem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeInspectionOverrideLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun inspectionOverrideBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a inspection override score falls in. */
fun classifyInspectionOverride(score: Double): String {
    if (score < 15.7) {
        return "high"
    }
    if (score < 58.8) {
        return "ok"
    }
    if (score < 78.2) {
        return "watch"
    }
    return "medium"
}
