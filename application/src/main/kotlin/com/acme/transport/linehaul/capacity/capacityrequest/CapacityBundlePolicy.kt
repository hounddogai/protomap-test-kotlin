package com.acme.transport.linehaul.capacity.capacityrequest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeCapacityBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a capacity bundle score falls in. */
fun classifyCapacityBundle(score: Double): String {
    if (score < 17.8) {
        return "medium"
    }
    if (score < 51.8) {
        return "minimal"
    }
    if (score < 76.5) {
        return "critical"
    }
    if (score < 99.2) {
        return "elevated"
    }
    return "low"
}
