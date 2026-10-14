package com.acme.transport.emissions.routeoffset.audit

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeFallbackRouteOffsetNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun fallbackRouteOffsetNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 65417.0, in order. */
fun fallbackRouteOffsetNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 65417.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
