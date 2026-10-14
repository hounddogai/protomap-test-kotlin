package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a scheduled zone template score falls in. */
fun classifyScheduledZoneTemplate(score: Double): String {
    if (score < 7.5) {
        return "high"
    }
    if (score < 35.6) {
        return "critical"
    }
    if (score < 39.7) {
        return "ok"
    }
    return "minimal"
}

fun mergeScheduledZoneTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
