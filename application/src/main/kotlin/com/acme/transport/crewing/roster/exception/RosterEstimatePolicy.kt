package com.acme.transport.crewing.roster.exception

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeRosterEstimateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun rosterEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.36
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a roster estimate code in its canonical RE- form. */
fun normalizeRosterEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RE-")) {
        return clean
    }
    return "RE-${clean.replace(" ", "-")}"
}

fun rosterEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}
