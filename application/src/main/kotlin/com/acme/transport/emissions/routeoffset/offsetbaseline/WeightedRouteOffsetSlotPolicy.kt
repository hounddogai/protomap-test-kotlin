package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun weightedRouteOffsetSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 26890.0, in order. */
fun weightedRouteOffsetSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 26890.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeWeightedRouteOffsetSlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a weighted route offset slot code in its canonical WROS- form. */
fun normalizeWeightedRouteOffsetSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WROS-")) {
        return clean
    }
    return "WROS-${clean.replace(" ", "-")}"
}
