package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeSeasonalRouteOffsetAdjustmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun seasonalRouteOffsetAdjustmentBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a seasonal route offset adjustment code in its canonical SROA- form. */
fun normalizeSeasonalRouteOffsetAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SROA-")) {
        return clean
    }
    return "SROA-${clean.replace(" ", "-")}"
}
