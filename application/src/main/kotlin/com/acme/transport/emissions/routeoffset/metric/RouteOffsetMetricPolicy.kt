package com.acme.transport.emissions.routeoffset.metric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeRouteOffsetMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a route offset metric code in its canonical ROM- form. */
fun normalizeRouteOffsetMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ROM-")) {
        return clean
    }
    return "ROM-${clean.replace(" ", "-")}"
}

fun routeOffsetMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.65
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
