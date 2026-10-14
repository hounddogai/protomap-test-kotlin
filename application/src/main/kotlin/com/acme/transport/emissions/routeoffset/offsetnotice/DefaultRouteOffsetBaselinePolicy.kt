package com.acme.transport.emissions.routeoffset.offsetnotice

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeDefaultRouteOffsetBaselineLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a default route offset baseline code in its canonical DROB- form. */
fun normalizeDefaultRouteOffsetBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DROB-")) {
        return clean
    }
    return "DROB-${clean.replace(" ", "-")}"
}

fun defaultRouteOffsetBaselineBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
