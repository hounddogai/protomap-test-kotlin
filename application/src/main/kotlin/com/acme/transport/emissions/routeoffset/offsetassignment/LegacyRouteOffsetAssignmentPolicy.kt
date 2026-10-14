package com.acme.transport.emissions.routeoffset.offsetassignment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeLegacyRouteOffsetAssignmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a legacy route offset assignment code in its canonical LROA- form. */
fun normalizeLegacyRouteOffsetAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LROA-")) {
        return clean
    }
    return "LROA-${clean.replace(" ", "-")}"
}

fun legacyRouteOffsetAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
