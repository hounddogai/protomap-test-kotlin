package com.acme.transport.safety.coaching.milestone

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun weightedCoachingTemplateBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a weighted coaching template score falls in. */
fun classifyWeightedCoachingTemplate(score: Double): String {
    if (score < 7.2) {
        return "minimal"
    }
    if (score < 34.4) {
        return "medium"
    }
    if (score < 69.6) {
        return "ok"
    }
    return "critical"
}

fun mergeWeightedCoachingTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
