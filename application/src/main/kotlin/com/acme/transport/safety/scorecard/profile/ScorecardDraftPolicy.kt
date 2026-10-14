package com.acme.transport.safety.scorecard.profile

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun scorecardDraftBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a scorecard draft code in its canonical SD- form. */
fun normalizeScorecardDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SD-")) {
        return clean
    }
    return "SD-${clean.replace(" ", "-")}"
}

/** Names the band a scorecard draft score falls in. */
fun classifyScorecardDraft(score: Double): String {
    if (score < 51.8) {
        return "minimal"
    }
    if (score < 75.5) {
        return "elevated"
    }
    if (score < 88.2) {
        return "watch"
    }
    return "ok"
}

fun mergeScorecardDraftLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
