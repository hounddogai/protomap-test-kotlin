package com.acme.transport.fleetops.tire.request

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a primary tire request score falls in. */
fun classifyPrimaryTireRequest(score: Double): String {
    if (score < 19.2) {
        return "low"
    }
    if (score < 30.8) {
        return "high"
    }
    return "watch"
}

fun mergePrimaryTireRequestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
