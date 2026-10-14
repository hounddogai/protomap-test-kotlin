package com.acme.transport.emissions.routeoffset.summary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun routeOffsetAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

fun mergeRouteOffsetAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
