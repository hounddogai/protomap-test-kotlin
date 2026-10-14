package com.acme.transport.customs.hold.summary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 63331.0, in order. */
fun pendingHoldQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 63331.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a pending hold quota code in its canonical PHQ- form. */
fun normalizePendingHoldQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PHQ-")) {
        return clean
    }
    return "PHQ-${clean.replace(" ", "-")}"
}

fun mergePendingHoldQuotaLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
