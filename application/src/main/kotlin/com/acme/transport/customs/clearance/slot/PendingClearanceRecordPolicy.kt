package com.acme.transport.customs.clearance.slot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Puts a pending clearance record code in its canonical PCR- form. */
fun normalizePendingClearanceRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PCR-")) {
        return clean
    }
    return "PCR-${clean.replace(" ", "-")}"
}

fun pendingClearanceRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.84
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergePendingClearanceRecordLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun pendingClearanceRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
