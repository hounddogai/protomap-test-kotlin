package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeCappedRouteOffsetMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 55183.0, in order. */
fun cappedRouteOffsetMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 55183.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a capped route offset metric code in its canonical CROM- form. */
fun normalizeCappedRouteOffsetMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CROM-")) {
        return clean
    }
    return "CROM-${clean.replace(" ", "-")}"
}
