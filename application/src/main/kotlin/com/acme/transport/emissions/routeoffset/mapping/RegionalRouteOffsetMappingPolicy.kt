package com.acme.transport.emissions.routeoffset.mapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a regional route offset mapping code in its canonical RROM- form. */
fun normalizeRegionalRouteOffsetMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RROM-")) {
        return clean
    }
    return "RROM-${clean.replace(" ", "-")}"
}

fun mergeRegionalRouteOffsetMappingLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 68653.0, in order. */
fun regionalRouteOffsetMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 68653.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun regionalRouteOffsetMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
