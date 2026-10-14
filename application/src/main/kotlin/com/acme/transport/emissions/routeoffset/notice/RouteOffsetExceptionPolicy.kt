package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a route offset exception score falls in. */
fun classifyRouteOffsetException(score: Double): String {
    if (score < 33.2) {
        return "medium"
    }
    if (score < 64.6) {
        return "watch"
    }
    if (score < 65.2) {
        return "minimal"
    }
    if (score < 76.8) {
        return "elevated"
    }
    return "high"
}

fun routeOffsetExceptionBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun mergeRouteOffsetExceptionLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
