package com.acme.transport.linehaul.tender.summary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeGlobalTenderDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun globalTenderDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Names the band a global tender digest score falls in. */
fun classifyGlobalTenderDigest(score: Double): String {
    if (score < 1.0) {
        return "elevated"
    }
    if (score < 7.2) {
        return "critical"
    }
    if (score < 17.5) {
        return "minimal"
    }
    if (score < 72.1) {
        return "watch"
    }
    return "high"
}
