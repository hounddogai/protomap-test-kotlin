package com.acme.transport.linehaul.tender.quota

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeGlobalTenderAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 48541.0, in order. */
fun globalTenderAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 48541.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a global tender allocation code in its canonical GTA- form. */
fun normalizeGlobalTenderAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GTA-")) {
        return clean
    }
    return "GTA-${clean.replace(" ", "-")}"
}

fun globalTenderAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
