package com.acme.transport.customs.inspection.audit

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 76128.0, in order. */
fun pendingInspectionBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 76128.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun pendingInspectionBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun mergePendingInspectionBatchLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a pending inspection batch code in its canonical PIB- form. */
fun normalizePendingInspectionBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PIB-")) {
        return clean
    }
    return "PIB-${clean.replace(" ", "-")}"
}
