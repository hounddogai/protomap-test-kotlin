package com.acme.transport.customs.hold.checkpoint

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 33381.0, in order. */
fun cappedHoldCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 33381.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a capped hold checkpoint code in its canonical CHC- form. */
fun normalizeCappedHoldCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CHC-")) {
        return clean
    }
    return "CHC-${clean.replace(" ", "-")}"
}

/** Names the band a capped hold checkpoint score falls in. */
fun classifyCappedHoldCheckpoint(score: Double): String {
    if (score < 8.6) {
        return "minimal"
    }
    if (score < 71.8) {
        return "watch"
    }
    if (score < 77.0) {
        return "medium"
    }
    return "low"
}

fun mergeCappedHoldCheckpointLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
