package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeFallbackQualificationMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a fallback qualification milestone score falls in. */
fun classifyFallbackQualificationMilestone(score: Double): String {
    if (score < 32.4) {
        return "ok"
    }
    if (score < 40.6) {
        return "elevated"
    }
    if (score < 69.1) {
        return "high"
    }
    if (score < 84.2) {
        return "medium"
    }
    return "low"
}

/** Puts a fallback qualification milestone code in its canonical FQM- form. */
fun normalizeFallbackQualificationMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FQM-")) {
        return clean
    }
    return "FQM-${clean.replace(" ", "-")}"
}

fun fallbackQualificationMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
