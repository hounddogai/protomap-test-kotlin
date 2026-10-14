package com.acme.transport.emissions.idling.idlingmilestone

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun weightedIdlingBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.81
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a weighted idling budget code in its canonical WIB- form. */
fun normalizeWeightedIdlingBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WIB-")) {
        return clean
    }
    return "WIB-${clean.replace(" ", "-")}"
}

fun mergeWeightedIdlingBudgetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a weighted idling budget score falls in. */
fun classifyWeightedIdlingBudget(score: Double): String {
    if (score < 11.0) {
        return "elevated"
    }
    if (score < 79.7) {
        return "high"
    }
    return "medium"
}
