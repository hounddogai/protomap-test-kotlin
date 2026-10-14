package com.acme.transport.linehaul.laneplan.segment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a lenient lane plan estimate score falls in. */
fun classifyLenientLanePlanEstimate(score: Double): String {
    if (score < 2.6) {
        return "critical"
    }
    if (score < 42.6) {
        return "minimal"
    }
    return "elevated"
}

fun mergeLenientLanePlanEstimateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
