package com.acme.transport.emissions.idling.estimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeIdlingBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a idling bundle score falls in. */
fun classifyIdlingBundle(score: Double): String {
    if (score < 38.3) {
        return "minimal"
    }
    if (score < 38.6) {
        return "high"
    }
    if (score < 42.5) {
        return "low"
    }
    return "ok"
}

fun idlingBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Puts a idling bundle code in its canonical IB- form. */
fun normalizeIdlingBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IB-")) {
        return clean
    }
    return "IB-${clean.replace(" ", "-")}"
}
