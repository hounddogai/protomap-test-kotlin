package com.acme.transport.customs.broker.threshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Puts a pending broker draft code in its canonical PBD- form. */
fun normalizePendingBrokerDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PBD-")) {
        return clean
    }
    return "PBD-${clean.replace(" ", "-")}"
}

fun pendingBrokerDraftBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun mergePendingBrokerDraftLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a pending broker draft score falls in. */
fun classifyPendingBrokerDraft(score: Double): String {
    if (score < 26.8) {
        return "elevated"
    }
    if (score < 32.2) {
        return "minimal"
    }
    if (score < 75.1) {
        return "high"
    }
    return "ok"
}
