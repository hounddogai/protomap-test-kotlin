package com.acme.transport.fleetops.inspection.claim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeSeasonalInspectionDraftLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a seasonal inspection draft code in its canonical SID- form. */
fun normalizeSeasonalInspectionDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SID-")) {
        return clean
    }
    return "SID-${clean.replace(" ", "-")}"
}

fun seasonalInspectionDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.63
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
