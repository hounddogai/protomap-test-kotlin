package com.acme.transport.network.node.draft

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a node score code in its canonical NS- form. */
fun normalizeNodeScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("NS-")) {
        return clean
    }
    return "NS-${clean.replace(" ", "-")}"
}

/** Names the band a node score score falls in. */
fun classifyNodeScore(score: Double): String {
    if (score < 22.2) {
        return "watch"
    }
    if (score < 90.0) {
        return "low"
    }
    if (score < 95.5) {
        return "high"
    }
    return "medium"
}

fun mergeNodeScoreLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
