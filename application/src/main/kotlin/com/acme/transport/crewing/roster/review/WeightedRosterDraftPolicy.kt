package com.acme.transport.crewing.roster.review

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun weightedRosterDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.9
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a weighted roster draft score falls in. */
fun classifyWeightedRosterDraft(score: Double): String {
    if (score < 9.0) {
        return "elevated"
    }
    if (score < 26.4) {
        return "critical"
    }
    if (score < 36.3) {
        return "minimal"
    }
    if (score < 91.3) {
        return "medium"
    }
    return "watch"
}

fun mergeWeightedRosterDraftLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a weighted roster draft code in its canonical WRD- form. */
fun normalizeWeightedRosterDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WRD-")) {
        return clean
    }
    return "WRD-${clean.replace(" ", "-")}"
}
