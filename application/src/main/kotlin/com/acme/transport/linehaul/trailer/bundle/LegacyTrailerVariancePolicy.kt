package com.acme.transport.linehaul.trailer.bundle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a legacy trailer variance score falls in. */
fun classifyLegacyTrailerVariance(score: Double): String {
    if (score < 24.3) {
        return "minimal"
    }
    if (score < 93.1) {
        return "critical"
    }
    return "watch"
}

fun mergeLegacyTrailerVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a legacy trailer variance code in its canonical LTV- form. */
fun normalizeLegacyTrailerVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LTV-")) {
        return clean
    }
    return "LTV-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 92700.0, in order. */
fun legacyTrailerVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 92700.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
