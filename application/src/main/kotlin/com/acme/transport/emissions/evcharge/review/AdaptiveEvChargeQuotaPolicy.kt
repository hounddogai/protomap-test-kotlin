package com.acme.transport.emissions.evcharge.review

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeAdaptiveEvChargeQuotaLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a adaptive ev charge quota code in its canonical AECQ- form. */
fun normalizeAdaptiveEvChargeQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AECQ-")) {
        return clean
    }
    return "AECQ-${clean.replace(" ", "-")}"
}

fun adaptiveEvChargeQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.61
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun adaptiveEvChargeQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
