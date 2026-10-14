package com.acme.transport.crewing.availability.request

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun availabilityTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.95
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeAvailabilityTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a availability target code in its canonical AT- form. */
fun normalizeAvailabilityTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AT-")) {
        return clean
    }
    return "AT-${clean.replace(" ", "-")}"
}

fun availabilityTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
