package com.acme.transport.emissions.routeoffset.summary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun routeOffsetSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

fun mergeRouteOffsetSummaryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
