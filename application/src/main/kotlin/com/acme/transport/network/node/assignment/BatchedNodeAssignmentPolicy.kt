package com.acme.transport.network.node.assignment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun batchedNodeAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Names the band a batched node assignment score falls in. */
fun classifyBatchedNodeAssignment(score: Double): String {
    if (score < 30.7) {
        return "minimal"
    }
    if (score < 46.8) {
        return "ok"
    }
    return "watch"
}

/** Puts a batched node assignment code in its canonical BNA- form. */
fun normalizeBatchedNodeAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BNA-")) {
        return clean
    }
    return "BNA-${clean.replace(" ", "-")}"
}

fun mergeBatchedNodeAssignmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
