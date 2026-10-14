package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeRouteOffsetQuotaLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a route offset quota score falls in. */
fun classifyRouteOffsetQuota(score: Double): String {
    if (score < 11.8) {
        return "ok"
    }
    if (score < 12.5) {
        return "low"
    }
    if (score < 56.4) {
        return "high"
    }
    if (score < 60.2) {
        return "minimal"
    }
    return "medium"
}
