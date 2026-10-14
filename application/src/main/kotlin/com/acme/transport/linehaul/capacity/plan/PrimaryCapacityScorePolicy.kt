package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergePrimaryCapacityScoreLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun primaryCapacityScoreBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a primary capacity score score falls in. */
fun classifyPrimaryCapacityScore(score: Double): String {
    if (score < 43.1) {
        return "critical"
    }
    if (score < 46.1) {
        return "medium"
    }
    if (score < 48.0) {
        return "watch"
    }
    return "minimal"
}

/** Puts a primary capacity score code in its canonical PCS- form. */
fun normalizePrimaryCapacityScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PCS-")) {
        return clean
    }
    return "PCS-${clean.replace(" ", "-")}"
}
