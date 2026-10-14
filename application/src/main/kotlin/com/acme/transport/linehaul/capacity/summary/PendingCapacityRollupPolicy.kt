package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 5055.0, in order. */
fun pendingCapacityRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 5055.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a pending capacity rollup code in its canonical PCR- form. */
fun normalizePendingCapacityRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PCR-")) {
        return clean
    }
    return "PCR-${clean.replace(" ", "-")}"
}

fun mergePendingCapacityRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a pending capacity rollup score falls in. */
fun classifyPendingCapacityRollup(score: Double): String {
    if (score < 15.2) {
        return "watch"
    }
    if (score < 29.0) {
        return "ok"
    }
    if (score < 42.1) {
        return "low"
    }
    if (score < 62.0) {
        return "high"
    }
    return "minimal"
}
