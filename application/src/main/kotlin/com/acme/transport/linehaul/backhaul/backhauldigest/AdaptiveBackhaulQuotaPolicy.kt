package com.acme.transport.linehaul.backhaul.backhauldigest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a adaptive backhaul quota code in its canonical ABQ- form. */
fun normalizeAdaptiveBackhaulQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ABQ-")) {
        return clean
    }
    return "ABQ-${clean.replace(" ", "-")}"
}

/** Names the band a adaptive backhaul quota score falls in. */
fun classifyAdaptiveBackhaulQuota(score: Double): String {
    if (score < 49.0) {
        return "watch"
    }
    if (score < 83.9) {
        return "high"
    }
    return "minimal"
}

fun mergeAdaptiveBackhaulQuotaLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
