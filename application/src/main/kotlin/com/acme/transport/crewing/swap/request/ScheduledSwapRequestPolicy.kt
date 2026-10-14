package com.acme.transport.crewing.swap.request

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeScheduledSwapRequestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a scheduled swap request score falls in. */
fun classifyScheduledSwapRequest(score: Double): String {
    if (score < 1.9) {
        return "high"
    }
    if (score < 34.7) {
        return "elevated"
    }
    if (score < 61.5) {
        return "ok"
    }
    return "low"
}

fun scheduledSwapRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Puts a scheduled swap request code in its canonical SSR- form. */
fun normalizeScheduledSwapRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SSR-")) {
        return clean
    }
    return "SSR-${clean.replace(" ", "-")}"
}
