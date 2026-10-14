package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a fallback parcel threshold score falls in. */
fun classifyFallbackParcelThreshold(score: Double): String {
    if (score < 3.1) {
        return "critical"
    }
    if (score < 47.4) {
        return "medium"
    }
    if (score < 62.7) {
        return "ok"
    }
    if (score < 99.6) {
        return "watch"
    }
    return "high"
}

fun mergeFallbackParcelThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
