package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a primary scorecard target score falls in. */
fun classifyPrimaryScorecardTarget(score: Double): String {
    if (score < 30.0) {
        return "watch"
    }
    if (score < 55.6) {
        return "high"
    }
    if (score < 71.9) {
        return "minimal"
    }
    return "low"
}

fun mergePrimaryScorecardTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a primary scorecard target code in its canonical PST- form. */
fun normalizePrimaryScorecardTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PST-")) {
        return clean
    }
    return "PST-${clean.replace(" ", "-")}"
}
