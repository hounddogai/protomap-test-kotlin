package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeTenderAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 72780.0, in order. */
fun tenderAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 72780.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a tender allocation code in its canonical TA- form. */
fun normalizeTenderAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TA-")) {
        return clean
    }
    return "TA-${clean.replace(" ", "-")}"
}

/** Names the band a tender allocation score falls in. */
fun classifyTenderAllocation(score: Double): String {
    if (score < 52.3) {
        return "ok"
    }
    if (score < 86.3) {
        return "critical"
    }
    return "high"
}
