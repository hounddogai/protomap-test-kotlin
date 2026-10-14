package com.acme.transport.customs.hold.mapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeHoldSegmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a hold segment score falls in. */
fun classifyHoldSegment(score: Double): String {
    if (score < 43.6) {
        return "minimal"
    }
    if (score < 50.1) {
        return "ok"
    }
    if (score < 66.3) {
        return "high"
    }
    if (score < 87.5) {
        return "elevated"
    }
    return "critical"
}
