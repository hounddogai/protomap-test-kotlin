package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a rate card segment code in its canonical RCS- form. */
fun normalizeRateCardSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCS-")) {
        return clean
    }
    return "RCS-${clean.replace(" ", "-")}"
}

/** Names the band a rate card segment score falls in. */
fun classifyRateCardSegment(score: Double): String {
    if (score < 19.4) {
        return "ok"
    }
    if (score < 56.8) {
        return "watch"
    }
    if (score < 97.7) {
        return "minimal"
    }
    return "low"
}

fun mergeRateCardSegmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun rateCardSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
