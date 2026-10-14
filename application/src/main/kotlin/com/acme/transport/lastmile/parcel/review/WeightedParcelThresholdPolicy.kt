package com.acme.transport.lastmile.parcel.review

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 10096.0, in order. */
fun weightedParcelThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 10096.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a weighted parcel threshold code in its canonical WPT- form. */
fun normalizeWeightedParcelThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WPT-")) {
        return clean
    }
    return "WPT-${clean.replace(" ", "-")}"
}

/** Names the band a weighted parcel threshold score falls in. */
fun classifyWeightedParcelThreshold(score: Double): String {
    if (score < 13.5) {
        return "minimal"
    }
    if (score < 72.6) {
        return "ok"
    }
    if (score < 77.6) {
        return "critical"
    }
    if (score < 93.2) {
        return "high"
    }
    return "low"
}

fun mergeWeightedParcelThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
