package com.acme.transport.linehaul.backhaul.quota

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a rolling backhaul quota score falls in. */
fun classifyRollingBackhaulQuota(score: Double): String {
    if (score < 40.5) {
        return "low"
    }
    if (score < 43.1) {
        return "medium"
    }
    if (score < 54.3) {
        return "watch"
    }
    if (score < 74.5) {
        return "high"
    }
    return "elevated"
}

fun rollingBackhaulQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Puts a rolling backhaul quota code in its canonical RBQ- form. */
fun normalizeRollingBackhaulQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RBQ-")) {
        return clean
    }
    return "RBQ-${clean.replace(" ", "-")}"
}

fun mergeRollingBackhaulQuotaLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
