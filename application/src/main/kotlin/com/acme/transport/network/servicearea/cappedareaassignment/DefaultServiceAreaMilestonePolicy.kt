package com.acme.transport.network.servicearea.cappedareaassignment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a default service area milestone score falls in. */
fun classifyDefaultServiceAreaMilestone(score: Double): String {
    if (score < 38.9) {
        return "low"
    }
    if (score < 80.0) {
        return "watch"
    }
    return "minimal"
}

fun mergeDefaultServiceAreaMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
