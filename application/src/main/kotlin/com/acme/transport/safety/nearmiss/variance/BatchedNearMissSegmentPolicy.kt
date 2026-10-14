package com.acme.transport.safety.nearmiss.variance

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a batched near miss segment score falls in. */
fun classifyBatchedNearMissSegment(score: Double): String {
    if (score < 5.9) {
        return "high"
    }
    if (score < 52.6) {
        return "low"
    }
    return "critical"
}

/** Puts a batched near miss segment code in its canonical BNMS- form. */
fun normalizeBatchedNearMissSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BNMS-")) {
        return clean
    }
    return "BNMS-${clean.replace(" ", "-")}"
}

fun mergeBatchedNearMissSegmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
