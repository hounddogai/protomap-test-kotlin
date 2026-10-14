package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeLenientDisputeTierLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a lenient dispute tier code in its canonical LDT- form. */
fun normalizeLenientDisputeTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LDT-")) {
        return clean
    }
    return "LDT-${clean.replace(" ", "-")}"
}

fun lenientDisputeTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.89
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun lenientDisputeTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
