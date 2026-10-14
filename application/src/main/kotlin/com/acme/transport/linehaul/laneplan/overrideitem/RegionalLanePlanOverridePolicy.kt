package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a regional lane plan override code in its canonical RLPO- form. */
fun normalizeRegionalLanePlanOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RLPO-")) {
        return clean
    }
    return "RLPO-${clean.replace(" ", "-")}"
}

fun mergeRegionalLanePlanOverrideLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun regionalLanePlanOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a regional lane plan override score falls in. */
fun classifyRegionalLanePlanOverride(score: Double): String {
    if (score < 1.9) {
        return "minimal"
    }
    if (score < 27.3) {
        return "ok"
    }
    if (score < 39.6) {
        return "high"
    }
    if (score < 91.4) {
        return "critical"
    }
    return "elevated"
}
