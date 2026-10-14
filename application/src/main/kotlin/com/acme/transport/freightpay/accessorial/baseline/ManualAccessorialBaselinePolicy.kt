package com.acme.transport.freightpay.accessorial.baseline

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a manual accessorial baseline score falls in. */
fun classifyManualAccessorialBaseline(score: Double): String {
    if (score < 38.7) {
        return "low"
    }
    if (score < 66.4) {
        return "ok"
    }
    if (score < 78.2) {
        return "critical"
    }
    if (score < 95.5) {
        return "medium"
    }
    return "high"
}

/** Puts a manual accessorial baseline code in its canonical MAB- form. */
fun normalizeManualAccessorialBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MAB-")) {
        return clean
    }
    return "MAB-${clean.replace(" ", "-")}"
}

fun mergeManualAccessorialBaselineLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
