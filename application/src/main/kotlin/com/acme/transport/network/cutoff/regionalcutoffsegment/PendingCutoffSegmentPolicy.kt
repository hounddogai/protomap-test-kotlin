package com.acme.transport.network.cutoff.regionalcutoffsegment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a pending cutoff segment code in its canonical PCS- form. */
fun normalizePendingCutoffSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PCS-")) {
        return clean
    }
    return "PCS-${clean.replace(" ", "-")}"
}

fun mergePendingCutoffSegmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a pending cutoff segment score falls in. */
fun classifyPendingCutoffSegment(score: Double): String {
    if (score < 7.9) {
        return "ok"
    }
    if (score < 47.7) {
        return "critical"
    }
    if (score < 88.7) {
        return "watch"
    }
    return "minimal"
}

/** Counts how many amounts fit in a budget of 42041.0, in order. */
fun pendingCutoffSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 42041.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
