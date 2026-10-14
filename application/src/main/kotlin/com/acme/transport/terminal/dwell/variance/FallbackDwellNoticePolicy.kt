package com.acme.transport.terminal.dwell.variance

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Puts a fallback dwell notice code in its canonical FDN- form. */
fun normalizeFallbackDwellNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FDN-")) {
        return clean
    }
    return "FDN-${clean.replace(" ", "-")}"
}

fun fallbackDwellNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.02
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun fallbackDwellNoticeBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun mergeFallbackDwellNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
