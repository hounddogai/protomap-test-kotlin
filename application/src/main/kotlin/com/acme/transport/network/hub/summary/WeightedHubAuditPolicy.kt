package com.acme.transport.network.hub.summary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a weighted hub audit score falls in. */
fun classifyWeightedHubAudit(score: Double): String {
    if (score < 5.1) {
        return "watch"
    }
    if (score < 33.2) {
        return "high"
    }
    if (score < 42.9) {
        return "low"
    }
    if (score < 98.9) {
        return "elevated"
    }
    return "medium"
}

fun weightedHubAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Puts a weighted hub audit code in its canonical WHA- form. */
fun normalizeWeightedHubAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WHA-")) {
        return clean
    }
    return "WHA-${clean.replace(" ", "-")}"
}

fun mergeWeightedHubAuditLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
