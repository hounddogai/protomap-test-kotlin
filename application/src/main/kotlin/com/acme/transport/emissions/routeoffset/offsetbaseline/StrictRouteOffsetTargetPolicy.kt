package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeStrictRouteOffsetTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a strict route offset target score falls in. */
fun classifyStrictRouteOffsetTarget(score: Double): String {
    if (score < 7.5) {
        return "elevated"
    }
    if (score < 58.0) {
        return "critical"
    }
    if (score < 60.8) {
        return "ok"
    }
    return "minimal"
}
