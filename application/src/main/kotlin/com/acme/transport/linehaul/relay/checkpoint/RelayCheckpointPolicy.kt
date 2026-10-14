package com.acme.transport.linehaul.relay.checkpoint

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a relay checkpoint score falls in. */
fun classifyRelayCheckpoint(score: Double): String {
    if (score < 28.5) {
        return "elevated"
    }
    if (score < 93.0) {
        return "watch"
    }
    return "critical"
}

/** Puts a relay checkpoint code in its canonical RC- form. */
fun normalizeRelayCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RC-")) {
        return clean
    }
    return "RC-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 78709.0, in order. */
fun relayCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 78709.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeRelayCheckpointLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
